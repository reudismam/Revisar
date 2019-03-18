package br.ufcg.spg.exp;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.database.EditDao;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.git.GitUtils;
import br.ufcg.spg.main.MainArguments;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;

public final class ExpUtils {

  private static List<String> completed = Arrays.asList(new String[] {"RxJava", "kafka"});

  private ExpUtils() {
  }

  public static List<String> getCompleted() {
    return completed;
  }
  
  /**
   * Gets projects.
   * 
   * @return projects
   */
  public static List<Tuple<String, String>> getProjects() {
    try {
      String projectsFile = MainArguments.getInstance().getProjects();
      final List<String> projects = Files.readLines(new File(projectsFile), 
          Charset.defaultCharset());
      return defineProjects(projects);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets projects.
   * 
   * @return projects
   */
  private static List<String> getAllProjects() {
    try {
      final List<String> projects = Files.readLines(new File("projects.txt"), 
          Charset.defaultCharset());
      final List<String> names = new ArrayList<>();
      for (final String project : projects) {
        names.add(project);
      }
      return names;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Gets projects.
   */
  public static void extractProjects() {
    try {
      List<String> allProjects = new ArrayList<>();
      for (int i = 1; i <= 10; i++) {
        String text = FileUtils.readFileToString(new File("repositories_page_" + i + ".txt"));
        List<String> allMatches = new ArrayList<>();
        Matcher m = Pattern.compile("clone_url\\\"\\: .*\\.git")
            .matcher(text);
        while (m.find()) {
          String project = m.group().substring(13);
          allMatches.add(project);
        }
        allProjects.addAll(allMatches);
      }
      StringBuilder bf = new StringBuilder();
      for (String project : allProjects) {
        String projectName = project.substring(19, project.lastIndexOf(".git"));
        String projectNameFiltered = projectName.replace('/', '_');
        bf.append("git clone ").append(project).append(' ').append(projectNameFiltered).append('\n');
      }
      FileUtils.writeStringToFile(new File("all_clones.txt"), bf.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Gets projects.
   */
  public static void saveStatisticsProjects() {
    try {
      final List<String> projects = Files.readLines(new File("projects.txt"), 
          Charset.defaultCharset());
      Map<String, Integer> map = new LinkedHashMap<>();
      
      for (final String project : projects) {
        File file = new File(project + ".txt");
        if (file.exists()) {
          final List<String> lines = Files.readLines(
              file, Charset.defaultCharset());
          map.put(project, lines.size());
          
        }
      }
      List<Map.Entry<String, Integer>> stde = sort(map);
      StringBuilder bf = new StringBuilder();
      for (Map.Entry<String, Integer> item : stde) {
        bf.append(item.getKey()).append(' ').append(item.getValue()).append('\n');
      }
      FileUtils.writeStringToFile(new File("statistics.txt"), bf.toString());
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  private static <K, V extends Comparable<? super V>> List<Entry<K, V>> sort(Map<K, V> map)     {
    List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
            return -1 * o1.getValue().compareTo(o2.getValue());
        }
    });
    return list;
  }

  /**
   * Return the e-mails in a list of commits.
   */
  public static List<String> allEmails() {
    final List<String> projects = getAllProjects();
    final Set<String> emails = new HashSet<>();
    final GitUtils git = new GitUtils();
    MainArguments main = MainArguments.getInstance();
    for (final String project : projects) {
      final String repoPath = main.getProjectFolder() + '/' + project + '/';
      try {
        final List<String> pemails = git.getEmail(repoPath);
        emails.addAll(pemails);
        ExpUtils.save(pemails, project + ".txt");
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
    return new ArrayList<>(emails);
  }
  
  /**
   * Return the e-mails in a list of commits.
   */
  public static List<String> filterCommits() {
    final List<String> projects = getAllProjects();
    final List<String> commits = new ArrayList<>();
    final GitUtils git = new GitUtils();
    for (final String project : projects) {
      final String repoPath = "../AllProjects/" + project + '/';
      try {
        final List<String> pcommits = git.filterCommits(repoPath);
        commits.addAll(pcommits);
        ExpUtils.save(pcommits, project + ".txt");
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
    return new ArrayList<>(commits);
  }

  /**
   * Shuffle a list.
   */
  public static List<String> shuffleList(final List<String> list) {
    final long seed = 15485863;
    Collections.shuffle(list, new Random(seed));
    return list;
  }

  /**
   * Save list of e-mails.
   * 
   * @param list
   *          of e-mails.
   * @param fileName
   *          file name.
   */
  public static void save(final List<String> list, final String fileName) throws IOException {
    try (FileWriter writer = new FileWriter(fileName)) {
      for (final String str : list) {
        writer.write(str + ",\n");
      }
    }
  }
  
  /**
   * Save list of e-mails.
   * 
   * @param map
   *          of e-mails.
   * @param fileName
   *          file name.
   */
  public static void save(final Map<String, Integer> map, final String fileName) throws IOException {
	Map<String, Integer> sorted = map.entrySet().stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    try (FileWriter writer = new FileWriter(fileName)) {
      for (final Entry<String, Integer> str : sorted.entrySet()) {
        writer.write(str.getKey() + ';' + str.getValue() + '\n');
      }
    }
  }

  private static List<Tuple<String, String>> defineProjects(final List<String> projects) {
    final List<Tuple<String, String>> projs = new ArrayList<>();
    for (int i = 0; i < projects.size(); i++) {
      final Tuple<String, String> tuple = new Tuple<>(projects.get(i).trim(), null);
      projs.add(tuple);
    }
    final Edit lastEdit = EditDao.getInstance().getLastEdit();
    if (lastEdit == null) {
      return projs;
    }
    String lastCommit = lastEdit.getCommit();
    String project = lastEdit.getDst().getProject();
    final List<Tuple<String, String>> remainPj = new ArrayList<>();
    boolean include = false;
    int index = projects.indexOf(project);
    if (index < projects.size() - 1 && completed.contains(project)) {
      project = projects.get(index + 1);
      lastCommit = null;
    }

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

  /**
   * Gets the log of a project.
   * 
   * @param pname
   *          name of the project
   * @return list of commit ids
   */
  public static List<String> getLogs(final String pname) throws IOException {
    // files to be analyzed
    String projectFolder = MainArguments.getInstance().getProjectFolder();
    final String projectFolderDst = projectFolder + "/" + pname + "/";
    final GitUtils analyzer = new GitUtils();
    List<String> log = null;
    // checkout the default branch
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
