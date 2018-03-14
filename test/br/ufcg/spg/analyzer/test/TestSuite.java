package br.ufcg.spg.analyzer.test;

import at.jku.risc.stout.urauc.algo.JustificationException;
import at.jku.risc.stout.urauc.util.ControlledException;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.cluster.UnifierCluster;
import br.ufcg.spg.config.TechniqueConfig;
import br.ufcg.spg.database.ClusterDao;
import br.ufcg.spg.database.EditDao;
import br.ufcg.spg.dependence.DependenceUtils;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.edit.EditStorage;
import br.ufcg.spg.exp.ExpUtils;
import br.ufcg.spg.git.GitUtils;
import br.ufcg.spg.log.TimeLogger;
import br.ufcg.spg.technique.Technique;
import br.ufcg.spg.transformation.TransformationUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.junit.Test;

public class TestSuite {
  
  @Test
  public void exp() throws IOException, JustificationException, ControlledException, CoreException {
    final TechniqueConfig config = TechniqueConfig.getInstance();
    config.setAllCommits(true);
    config.setEditsToAnalyze(100);
    config.setTemplateIncludesType(false);
    final List<Tuple<String, String>> projects = ExpUtils.getProjects();
    testBaseTableExpProjects(projects);
  }
  
  @Test
  public void exp_Cluster() 
      throws IOException, JustificationException, ControlledException, CoreException {
    Technique.clusterEdits();
    Technique.translateEdits();
    System.out.println("END.");
  }
  
  @Test
  public void exp_Dependence() 
      throws MissingObjectException, IncorrectObjectTypeException, 
      AmbiguousObjectException, IOException, ExecutionException,
      NoFilepatternException, GitAPIException {
    DependenceUtils.dependences();
  }
  
  @Test
  public void exp_Translate() 
      throws IOException, JustificationException, ControlledException, CoreException {
    Technique.translateEdits();
    System.out.println("END.");
  }
  
  @Test
  public void exp_TranslateId() 
      throws IOException, JustificationException, ControlledException, CoreException {
    Technique.translateEdits("29414");
    System.out.println("END.");
  }
  
  @Test
  public void test_d_cap() 
      throws IOException, JustificationException, ControlledException, CoreException {
    final ClusterDao dao = ClusterDao.getInstance();
    final List<Cluster> clusters = dao.getSrcEdits("113406");
    final Cluster cluster = clusters.get(0);
    final Edit srcEdit = cluster.getNodes().get(0);
    final String srcDcap = srcEdit.getDcap3();
    final String dstDcap = srcEdit.getDst().getDcap3();
    final EditDao editDao = EditDao.getInstance();
    final List<Edit> srcList = editDao.getSrcEditsByDcap(srcDcap, dstDcap,  3);
    final Map<String, List<Edit>> groups = 
        srcList.stream().collect(Collectors.groupingBy(w -> w.getDst().getDcap3()));
    for (final Entry<String, List<Edit>> entry: groups.entrySet()) {
      final List<Edit> toAnalyze = entry.getValue();
      final List<Cluster> clts =  
          UnifierCluster.getInstance().clusters(toAnalyze);
      final ClusterDao cdao = ClusterDao.getInstance();
      cdao.saveAll(clts);     
      TransformationUtils.transformations(clts);
    }
    System.out.println("END.");
  }
  
  @Test
  public void get_all_commits_cluster() {
    final ClusterDao dao = ClusterDao.getInstance();
    final List<String> commits = dao.getAllCommitsClusters();
    final String lastCommit = "1447e596aa13ca3441f24a8e163f4a255c5a7e23";
    final int index = commits.indexOf(lastCommit);
    final double size = commits.size();
    for (int i = index + 1; i < commits.size(); i++) {
      final String commit = commits.get(i);
      System.out.println(i / size + "% calculate : current commit: " + commit);
      try {
        DependenceUtils.computeGraph(commit);
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
  }
 
  public void getEmails() throws IOException, NoHeadException, GitAPIException {
    final List<String> emails = ExpUtils.allEmails();
    ExpUtils.saveEmails(emails, "email.txt");
    final List<String> shiffle = ExpUtils.shuffleList(emails);
    ExpUtils.saveEmails(shiffle, "shuffle_email.txt");
  }

  /**
   * Common test method.
   * @param project project
   */
  public void testBaseTableExp(final Tuple<String, String> project)
      throws IOException, JustificationException, ControlledException, CoreException {
    // files to be analyzed
    final String projectFolderDst = "../Projects/" + project.getItem1() + "/";
    final GitUtils analyzer = new GitUtils();
    final List<String> log = ExpUtils.getLogs(project.getItem1());
    final EditStorage storage = EditStorage.getInstance();
    final int startCount = storage.getNumberEdits();
    final int numberToAnalyze = TechniqueConfig.getInstance().getEditsToAnalyze();
    final int max = startCount + numberToAnalyze;
    storage.setMaxNumberEdits(max);
    int index = 5;
    if (project.getItem2() != null) {
      index = log.indexOf(project.getItem2());
      if (index == -1) {
        throw new RuntimeException("Invalid index of commits!!!");
      }
      index++;
    }
    for (int i = index; i < log.size(); i++) {
      System.out.println(((double) i) / log.size() + " % completed");
      final String dstCommit = log.get(i);
      final List<String> files = analyzer.modifiedFiles(projectFolderDst, dstCommit);
      //if there is no previous commit.
      if (files == null) {
        return;
      }
      storage.setCurrentCommit(dstCommit);
      storage.addCommitProject(project.getItem1(), dstCommit);
      Technique.addEdits(project.getItem1(), files, dstCommit);
      System.out.print("PROCESSED NODES SO FAR:");
      final int currentCount = storage.getNumberEdits();
      System.out.println(currentCount);
      final String pname = project.getItem1();
      System.out.println("DEBUG COMMITS: " + storage.getCommitProjects().get(pname).size());
      System.out.println("DEBUG CURRENT COMMIT: " + dstCommit);
      if (currentCount >= max && !TechniqueConfig.getInstance().isAllCommits()) {
        return;
      }
    }
  }

  /**
   * Test for many projects.
   * @param projects projects.
   */
  public void testBaseTableExpProjects(final List<Tuple<String, String>> projects)
      throws IOException, JustificationException, ControlledException, CoreException {
    final long startTime = System.nanoTime();     
    for (final Tuple<String, String> project : projects) {
      testBaseTableExp(project);
    }
    final long estimatedTime = System.nanoTime() - startTime;
    TimeLogger.getInstance().setTimeExtract(estimatedTime);
    Technique.clusterEdits();
    Technique.translateEdits();
    System.out.println("DEBUG: TOTAL COMMITS");
    final EditStorage storage = EditStorage.getInstance();
    for (final Tuple<String, String> project: projects) {
      System.out.println("=====================");
      System.out.println(project.getItem1());
      System.out.println("Total: " + storage.getCommitProjects().get(project.getItem1()).size());
      System.out.println("=====================");     
    }
    System.out.println("END.");
  }

  /**
   * Test base method.
   * @throws ExecutionException 
   * 
   */
  public void testBaseTable(final String project, final List<String> files)
      throws IOException, JustificationException, ControlledException, 
      CoreException, ExecutionException {
    testBaseTable(project, files, "");
  }
  
  /**
   * Test base method.
   * 
   */
  public void testBaseTable(final String project, final String commit)
      throws IOException, JustificationException, ControlledException, CoreException {
    Technique.addEdits(project, commit);
    Technique.clusterEdits();
    Technique.translateEdits();
  }
  
  

  /**
   * Test base method.
   * @throws ExecutionException 
   * 
   */
  public void testBaseTable(final String project, final List<String> files, final String commit)
      throws IOException, JustificationException, ControlledException, 
      CoreException, ExecutionException {
    // Computing before after edits
    Technique.addEdits(project, files, commit);
    Technique.clusterEdits();
    Technique.translateEdits();
  }
}