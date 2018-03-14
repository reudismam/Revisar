package br.ufcg.spg.exp;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.database.EditDao;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.git.GitUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import com.google.common.io.Files;

public class ExpUtils {
  
  /**
   * Gets projects.
   * @return projects
   */
  public static List<Tuple<String, String>> getProjects() {
    final String[] projects = {"ant", "hive", "drill", "gson", "ExoPlayer", "giraph", "error-prone", "guava", "maven", "truth"};
    //final String[] projects = {"ant"};//{"ant", "drill", "error-prone", "guava", "maven", "truth"};
    final List<Tuple<String, String>> projs = defineProjects(projects);
    return projs;
  }
  
  /**
   * Gets projects.
   * @return projects
   */
  public static List<String> getAllProjects() {
    try {
      final List<String> projects = Files.readLines(new File("projects.txt"), 
          Charset.defaultCharset());
      final List<String> names = new ArrayList<>();
      for (final String project : projects) {
        final String pname = project.substring(project.lastIndexOf('/') + 1, project.length() - 4);
        names.add(pname);
      }
      return names;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public static List<String> allEmails() throws NoHeadException, IOException, GitAPIException {
    final List<String> projects = getAllProjects();
    final Set<String> emails = new HashSet<>();
    final GitUtils git = new GitUtils();
    for (final String project : projects) {
      final String repoPath = "../AllProjects/" + project + "/";
      try {
        final List<String> pemails = git.getEmail(repoPath);
        emails.addAll(pemails);
        ExpUtils.saveEmails(pemails, project + ".txt");
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
    return new ArrayList<>(emails);
  }
  
  public static List<String> shuffleList(final List<String> list) {
    final long seed = 15485863;
    Collections.shuffle(list, new Random(seed));
    return list;
  }
  
  public static void saveEmails(final List<String> list, final String fileName) throws IOException {
    final FileWriter writer = new FileWriter(fileName); 
    for (final String str: list) {
      writer.write(str + ",\n");
    }
    writer.close();
  }
  
  private static List<Tuple<String, String>> defineProjects(final String[] projects) {
    final List<Tuple<String, String>> projs = new ArrayList<>();
    for (int i = 0; i < projects.length; i++) {
      final Tuple<String, String> tuple = new Tuple<>(projects[i], null);
      projs.add(tuple);
    }
    //projs.get(0).setItem2("ee47fa6e1a3828f7f52d4c3a8307074603e70740");
    final Edit lastEdit = EditDao.getInstance().getLastEdit();
    if (lastEdit == null) {
      return projs;
    }
    final String lastCommit = lastEdit.getCommit();
    final String project = lastEdit.getDst().getProject();
    final List<Tuple<String, String>> remainPj = new ArrayList<>();
    boolean include = false;
    for (final Tuple<String, String> pj : projs) {
      if (pj.getItem1().equals(project)) {
        include = true;
        pj.setItem2(lastCommit);
      }
      if (include) {
        remainPj.add(pj);
      }
    }
    return remainPj;
  }
  
  public static List<String> getLogs(final String pname) throws IOException {
    // files to be analyzed
    final String projectFolderDst = "../Projects/" + pname  + "/";
    final GitUtils analyzer = new GitUtils();
    List<String> log = null;
    //checkout the default branch
    try {
      GitUtils.checkout(projectFolderDst, "refs/remotes/origin/HEAD");
    } catch (IOException | GitAPIException e) {
      e.printStackTrace();
    }
    try {
      log = analyzer.gitLog(projectFolderDst);
    } catch (final GitAPIException e1) {
      e1.printStackTrace();
    }
    return log;
  }
}
