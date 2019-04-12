package br.ufcg.spg.analyzer.test;

import br.ufcg.spg.bean.EditFile;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.cli.CLI;
import br.ufcg.spg.cli.CheckStyleReport;
import br.ufcg.spg.cli.PMDUtils;
import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.cluster.ClusterUnifier;
import br.ufcg.spg.cluster.ClusterUtils;
import br.ufcg.spg.config.TechniqueConfig;
import br.ufcg.spg.database.ClusterDao;
import br.ufcg.spg.database.DependenceDao;
import br.ufcg.spg.database.EditDao;
import br.ufcg.spg.dependence.DependenceUtils;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.edit.EditStorage;
import br.ufcg.spg.emerging.EmergingPatternsUtils;
import br.ufcg.spg.exp.ExpUtils;
import br.ufcg.spg.git.GitUtils;
import br.ufcg.spg.log.TimeLogger;
import br.ufcg.spg.main.MainArguments;
import br.ufcg.spg.stub.StubUtils;
import br.ufcg.spg.technique.Technique;
import br.ufcg.spg.technique.TechniqueUtils;
import br.ufcg.spg.transformation.Transformation;
import br.ufcg.spg.transformation.TransformationUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

public class TestSuite {

  private static final Logger logger = LogManager.getLogger(TestSuite.class.getName());

  /**
   * Constructor.
   */
  public TestSuite() {
    //Empty constructor.
  }

  private void configMainArguments() {
    final MainArguments arguments = MainArguments.getInstance();
    arguments.setProjects("projects.txt");
    arguments.setProjectFolder("../Projects");
  }
  
  @Before
  public void setup() {
    configMainArguments();
  }

  @Test
  public void extractEdits() 
      throws IOException {
    try {
      final TechniqueConfig config = TechniqueConfig.getInstance();
      config.setAllCommits(true);
      config.setEditsToAnalyze(100);
      config.setTemplateIncludesType(false);
      final List<Tuple<String, String>> projects = ExpUtils.getProjects();
      testBaseTableExpProjects(projects);
    } catch (OutOfMemoryError e) {
      logger.trace(e.getMessage());
      System.out.println();
      EditStorage.clear();
      extractEdits();
    }
  }
  
  @Test
  public void clusterEdits() {
    Technique.clusterEdits();
    Technique.translateEdits();
    logger.trace("END.");
  }
  
  @Test
  public void translateClusters() {
    Technique.translateEdits();
    logger.trace("END.");
  }

  @Test
  public void testFindBugs() {
    try {
      StubUtils.generateStubsForClass("temp.java");
    } catch (Exception e) {
      e.printStackTrace();
    }
    List<String> lines =  CLI.runCommandLine("javac -classpath . -bootclasspath \"C://Program Files/Java/jre1.8.0_202/lib/rt.jar\" defaultpkg/temp.java");
    if (!lines.isEmpty()) {
      throw new RuntimeException();
    }
    CLI.runCommandLine("jar cf myJar.jar defaultpkg/temp.class");
    for (String line : lines) {
      System.out.println(line);
    }
  }

  @Test
  public void translateMachineLearningClustersMoreProjects() {
    List<Cluster> clusters = ClusterDao.getClusterMoreProjects();
    int i = clusters.size();
    logger.trace(i);
    TransformationUtils.transformationsMoreProjects(clusters);
    logger.trace("END.");
  }

  @Test
  public void dependenceFromEdits() throws 
      IOException, ExecutionException, GitAPIException {
    DependenceUtils.dependences();
  }

  @Test
  public void translateNewClustersMoreProjects() {
    Map<String, List<Edit>> dcaps = groupEditByDcap();
    List<Cluster> clustersDcap = new ArrayList<>();
    for (Entry<String, List<Edit>> entry : dcaps.entrySet()) {
      List<Cluster> clusterForDcap = ClusterUnifier.getInstance().clusterEdits(entry.getValue());
      clustersDcap.addAll(clusterForDcap);
    }
    TransformationUtils.transformationsMoreProjects(clustersDcap);
    logger.trace("END.");
  }

  private Map<String, List<Edit>> groupEditByDcap() {
    List<Cluster> clusters = ClusterDao.getClusterMoreProjects();
    List<Edit> allEdits = new ArrayList<>();
    int i = clusters.size();
    logger.trace(i);
    for (Cluster c : clusters) {
      allEdits.addAll(c.getNodes());
    }
    return ClusterUnifier.getInstance().groupEditsByDCap(allEdits,
            TechniqueConfig.getInstance());
  }
  
  @Test
  public void translateClustersMoreProjectsByDcap() {
    Map<String, List<Edit>> dcaps = groupEditByDcap();
    List<Cluster> clustersDcap = new ArrayList<>();
    for (Entry<String, List<Edit>> entry : dcaps.entrySet()) {
      List<Cluster> clusterForDcap = ClusterUnifier.getInstance().clusterEdits(entry.getValue());
      TransformationUtils.transformationsMoreProjects(clusterForDcap);
      clustersDcap.addAll(clusterForDcap);
    }
    TransformationUtils.transformationsMoreProjects(clustersDcap);
    logger.trace("END.");
  }

  @Test
  public void translateClustersMoreProjects() {
    List<Cluster> clusters = ClusterDao.getClusterMoreProjects();
    int i = clusters.size();
    logger.trace(i);
    TransformationUtils.transformations(clusters);
    logger.trace("END.");
  }
  
  @Test
  public void emergingPatterns() {
    List<Cluster> clusters = ClusterDao.getClusterMoreProjects();
    int i = clusters.size();
    logger.trace(i);
    EmergingPatternsUtils.emergingPatterns(clusters);
    logger.trace("END.");
  }
  
  @Test
  public void buildRefasterRules() {
    List<Cluster> clusters = ClusterDao.getClusterMoreProjects();
    int j = clusters.size();
    logger.trace(j);
    int i = 0;
    for (Cluster cluster : clusters) {
      i++;
      try {
        final List<String> refasterRules = new ArrayList<>();
        final Edit edit = cluster.getNodes().get(0);
        final Transformation transformation = TransformationUtils.tranformation(cluster);
        final String refaster = TransformationUtils.createRefasterRule(cluster, edit);
        refasterRules.add(refaster);
        String counterFormated =  String.format("%03d", i);
        MainArguments main = MainArguments.getInstance();
        String path = main.getProjectFolder() + TransformationUtils.CLUSTER_PATH +  counterFormated + '/';
        TransformationUtils.saveTransformation(path, transformation, edit); 
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    logger.trace("END.");
  }
  
  @Test
  public void buildAllPairsOfEdits() {
    List<Cluster> clusters = ClusterDao.getClusterMoreProjects();
    int j = clusters.size();
    logger.trace(j);
    List<Edit> allEdits = ClusterUtils.getAllEdits(clusters);
    Set<Tuple<Edit, Edit>> pairs = new HashSet<>();
    for (Edit editi: allEdits) {
      for (Edit editj: allEdits) {
        if (!(editi == editj || pairs.contains(new Tuple<>(editj, editi)))) {
          pairs.add(new Tuple<>(editi, editj));
          Cluster srcCluster = new Cluster(editi.getTemplate(), "");
          srcCluster.getNodes().add(editi);
          Cluster dstCluster = new Cluster(editi.getDst().getTemplate(), "");
          dstCluster.getNodes().add(editi.getDst());
          srcCluster.setDst(dstCluster);
          boolean isValid = ClusterUnifier.getInstance()
              .isValid(srcCluster, dstCluster, editj);
          if (isValid) {
            String path = "../Projects/cluster/pairs_of_edits.txt";
            final File clusterFile = new File(path);
            String content = "\n============BEGIN PAIR========================\n";
            content += "ID: " + editi.getId() + " " + editi.getText() + " => " 
              + editi.getDst().getText() + "\n";
            content += "ID: " + editj.getId() + " " + editj.getText() + " => " 
              + editj.getDst().getText();
            content += "\n============END PAIR========================\n\n";
            try {
              if (!clusterFile.exists()) {
                clusterFile.createNewFile();
              }
              Files.write(clusterFile.toPath(), content.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        }
      }
    }
    logger.trace("END.");
  }

  @Test
  public void buildRefasterRulesAllEdits() {
    List<Cluster> clusters = ClusterDao.getClusterMoreProjects();
    int j = clusters.size();
    logger.trace(j);
    int i = 0;
    for (Cluster clusteri : clusters) {
      i++;
      try {
        final List<Cluster> srcClusters = ClusterUtils.segmentByType(clusteri);
        for (final Cluster cluster : srcClusters) {
          final List<String> refasterRules = new ArrayList<>();
          for (final Edit edit : cluster.getNodes()) {
            final Transformation transformation = TransformationUtils.tranformation(cluster);
            final String refaster = TransformationUtils.createRefasterRule(cluster, edit);
            refasterRules.add(refaster);
            String counterFormated =  String.format("%03d", i);
            String path = "../Projects/cluster/" +  counterFormated + "/";
            TransformationUtils.saveTransformation(path, transformation, edit);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    logger.trace("END.");
  }

  @Test
  public void translateClusterById() {
    Technique.translateEdits("16471");
    logger.trace("END.");
  }

  @Test
  public void buildClusterById() {
    ClusterUtils.buildClusters("1290970");
    logger.trace("END.");
  }

  @Test
  public void clusterMoreProjectsByDCap() {
    List<Cluster> clusters = ClusterDao.getClusterMoreProjects();
    int i = clusters.size();
    logger.trace(i);
    List<Edit> allEdits = ClusterUtils.getAllEdits(clusters);
    Map<String, List<Edit>> dcaps = ClusterUnifier.getInstance().groupEditsByDCap(allEdits,
        TechniqueConfig.getInstance());
    List<Cluster> clustersDcap = new ArrayList<>();
    for (Entry<String, List<Edit>> entry : dcaps.entrySet()) {
      List<Cluster> clusterForDcap = ClusterUnifier.getInstance().clusterEdits(entry.getValue());
      clustersDcap.addAll(clusterForDcap);
    }
    TransformationUtils.transformations(clustersDcap);
    logger.trace("END.");
  }

  @Test
  public void clusterEditsNoDcap() {
    List<Cluster> clusters = ClusterDao.getClusterMoreProjects();
    List<Edit> allEdits = new ArrayList<>();
    int i = clusters.size();
    logger.trace(i);
    for (Cluster c : clusters) {
      allEdits.addAll(c.getNodes());
    }
    List<Cluster> newClusters = ClusterUnifier.getInstance().clusterEdits(allEdits);
    TransformationUtils.transformations(newClusters);
    logger.trace("END.");
  }

  @Test
  public void testDCap() {
    final ClusterDao dao = ClusterDao.getInstance();
    final List<Cluster> clusters = dao.getClusters("113406");
    final Cluster cluster = clusters.get(0);
    final Edit srcEdit = cluster.getNodes().get(0);
    final String srcDcap = srcEdit.getDcap3();
    final String dstDcap = srcEdit.getDst().getDcap3();
    final EditDao editDao = EditDao.getInstance();
    final List<Edit> srcList = editDao.getSrcEditsByDcap(srcDcap, dstDcap, 3);
    final Map<String, List<Edit>> groups = 
        srcList.stream().collect(Collectors.groupingBy(w -> w.getDst().getDcap3()));
    for (final Entry<String, List<Edit>> entry : groups.entrySet()) {
      final List<Edit> toAnalyze = entry.getValue();
      final List<Cluster> clts = ClusterUnifier.getInstance().clusters(toAnalyze);
      final ClusterDao cdao = ClusterDao.getInstance();
      cdao.saveAll(clts);
      TransformationUtils.transformations(clts);
    }
    logger.trace("END.");
  }

  @Test
  public void learningDependenceClusters() {
    final ClusterDao dao = ClusterDao.getInstance();
    final List<String> commits = dao.getAllCommitsClusters();
    DependenceDao dependenceDao = DependenceDao.getInstance();
    final Edit edit = dependenceDao.lastDependence();
    final String lastCommit = edit.getCommit();
    final int index = commits.indexOf(lastCommit);
    final double size = commits.size();
    for (int i = index + 1; i < commits.size(); i++) {
      final String commit = commits.get(i);
      logger.trace((i * 1.0 / size) * 100 + "% calculate : current commit: " + commit);
      try {
        DependenceUtils.computeGraph(commit);
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Gets e-mails.
   */
  public void getEmails() throws IOException {
    final List<String> emails = ExpUtils.allEmails();
    ExpUtils.save(emails, "email.txt");
    final List<String> shiffle = ExpUtils.shuffleList(emails);
    ExpUtils.save(shiffle, "shuffle_email.txt");
  }
  
  /**
   * Gets e-mails.
   */
  @Test
  public void filterCommitsByMessage() throws IOException {
    final List<String> filtered = ExpUtils.filterCommits();
    ExpUtils.save(filtered, "filtered_commits.txt");
    //final List<String> shiffle = ExpUtils.shuffleList(emails);
    //ExpUtils.save(shiffle, "shuffle_email.txt");
  }
  
  /**
   * Gets e-mails.
   */
  @Test
  public void getStatistics() {
    ExpUtils.saveStatisticsProjects();
  }
  
  /**
   * Gets e-mails.
   */
  @Test
  public void extractProjects() {
    ExpUtils.extractProjects();
  }

  /**
   * Test for many projects.
   * 
   * @param projects
   *          projects.
   */
  public void testBaseTableExpProjects(final List<Tuple<String, String>> projects)
      throws IOException {
    final long startTime = System.nanoTime();
    for (final Tuple<String, String> project : projects) {
      TechniqueUtils.concreteEdits(project);
    }
    final long estimatedTime = System.nanoTime() - startTime;
    TimeLogger.getInstance().setTimeExtract(estimatedTime);
    Technique.clusterEdits();
    Technique.translateEdits();
    logger.trace("DEBUG: TOTAL COMMITS");
    final EditStorage storage = EditStorage.getInstance();
    for (final Tuple<String, String> project : projects) {
      logger.trace("=====================");
      logger.trace(project.getItem1());
      logger.trace("TOTAL: " + storage.getCommitProjects().get(project.getItem1()).size());
      logger.trace("=====================");
    }
    logger.trace("END.");
  }

  /**
   * Test base method.
   * 
   */
  public void testBaseTable(final String project, final List<EditFile> files)
      throws IOException {
    testBaseTable(project, files, "");
  }

  @Test
  public void reports() {
    List<CheckStyleReport> reports = PMDUtils.getPMDReports("temp1.java");
    for (CheckStyleReport report : reports) {
      System.out.println(report);
    }
  }

  /**
   * Test base method.
   * 
   */
  public void testBaseTable(final String project, final String hashId)
      throws IOException {
    configMainArguments();
    RevCommit commit = GitUtils.extractCommit(MainArguments.getInstance()
        .getProjectFolder() + "/" + project, hashId);
    Technique.addEdits(project, commit);
    Technique.clusterEdits();
    Technique.translateEdits();
  }

  /**
   * Test base method.
   * 
   */
  public void testBaseTable(final String project, final List<EditFile> files, final String hashId)
      throws IOException {
    RevCommit commit = GitUtils.extractCommit(MainArguments.getInstance()
        .getProjectFolder() + "/" + project, hashId);
    // Computing before after edits
    Technique.addEdits(project, files, commit);
    Technique.clusterEdits();
    Technique.translateEdits();
  }
}