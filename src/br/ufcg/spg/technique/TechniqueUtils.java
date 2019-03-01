package br.ufcg.spg.technique;

import br.ufcg.spg.analyzer.test.TestSuite;
import br.ufcg.spg.bean.EditFile;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.config.TechniqueConfig;
import br.ufcg.spg.edit.EditStorage;
import br.ufcg.spg.exp.ExpUtils;
import br.ufcg.spg.git.GitUtils;
import br.ufcg.spg.main.MainArguments;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.revwalk.RevCommit;


public class TechniqueUtils {
  
  private static final Logger logger = LogManager.getLogger(TestSuite.class.getName());
  
  private TechniqueUtils() {
  }
  
  /**
   * Extract concrete edits.
   * @param project project
   */
  public static void concreteEdits(final Tuple<String, String> project)
      throws IOException {
    // files to be analyzed
    final String projectFolderDst = "../Projects/" + project.getItem1() + "/";
    final GitUtils analyzer = new GitUtils();
    ExpUtils.getLogs(project.getItem1());
    final List<RevCommit> log = GitUtils.gitRevCommitLog(MainArguments.getInstance()
        .getProjectFolder() + "/" + project.getItem1());
    final EditStorage storage = EditStorage.getInstance();
    final int startCount = storage.getNumberEdits();
    final int numberToAnalyze = TechniqueConfig.getInstance().getEditsToAnalyze();
    final int max = startCount + numberToAnalyze;
    storage.setMaxNumberEdits(max);
    int index = 5;
    if (project.getItem2() != null) {
      index = indexOf(project.getItem2(), log);
      if (index == -1) {
        throw new RuntimeException("Invalid index of commits!!!");
      }
      index++;
    }
    for (int i = index; i < log.size(); i++) {
      logger.trace(((double) i) / log.size() + " % completed");
      final RevCommit dstCommit = log.get(i);
      List<EditFile> files;
      try {
        files = analyzer.modifiedFiles(projectFolderDst, dstCommit);
      } catch (Exception e) {
        logger.trace("Large commit. IGNORE");
        continue;
      }
      //if there is no previous commit.
      if (files == null) {
        return;
      }
      storage.setCurrentCommit(dstCommit);
      storage.addCommitProject(project.getItem1(), dstCommit);
      Technique.addEdits(project.getItem1(), files, dstCommit);
      logger.trace("PROJECT: " + project.getItem1());
      final int currentCount = storage.getNumberEdits();
      logger.trace("NODE PROCESSED: " + currentCount);
      final String pname = project.getItem1();
      logger.trace("DEBUG COMMITS: " + storage.getCommitProjects().get(pname).size());
      logger.trace("DEBUG CURRENT COMMIT: " + dstCommit);
      if (currentCount >= max && !TechniqueConfig.getInstance().isAllCommits()) {
        return;
      }
    }
  }
  
  private int status;
  
  /**
   * Gets the difference between source code and destination code.
   */
  public void modifiedFiles(RevCommit dstCommit, String projectFolderDst, 
      final Tuple<String, String> project) {
    status = -1;
    tryProcessCommit(dstCommit, projectFolderDst, project);
    if (status != -1) {
      return;
    }
    throw new RuntimeException("Long time to process commit\n.");
  } 

  /**
   * Try to unify eq1 and eq2.
   */
  public void tryProcessCommit(RevCommit dstCommit, String projectFolderDst, 
      final Tuple<String, String> project) {
    final ExecutorService executor = Executors.newFixedThreadPool(4);
    final Future<?> future = executor.submit(new Runnable() {
      /**
       * Run method.
       */
      @Override
      public void run() {
        status = processCommit(dstCommit, projectFolderDst, project);
      }
    });
    executor.shutdown(); // <-- reject all further submissions
    try {
      future.get(60, TimeUnit.SECONDS); // <-- wait 30 seconds to finish
    } catch (final InterruptedException e) { // <-- possible error cases
      logger.trace("job was interrupted");
    } catch (final ExecutionException e) {
      logger.trace("caught exception: " + e.getCause());
    } catch (final TimeoutException e) {
      future.cancel(true); // <-- interrupt the job
      logger.trace("timeout");
    }
    // wait all unfinished tasks for 2 sec
    try {
      if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
        // force them to quit by interrupting
        executor.shutdownNow();
      }
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
    throw new RuntimeException("Long time to process commit.\n");
  }
  
  /**
   * Process an commit.
   */
  public int processCommit(RevCommit dstCommit, String projectFolderDst, 
      final Tuple<String, String> project) {
    final GitUtils analyzer = new GitUtils();
    final EditStorage storage = EditStorage.getInstance();
    List<EditFile> files;
    try {
      files = analyzer.modifiedFiles(projectFolderDst, dstCommit);
    } catch (Exception e) {
      e.printStackTrace();
      return - 1;
    }
    //if there is no previous commit.
    if (files == null) {
      return - 1;
    }
    storage.setCurrentCommit(dstCommit);
    storage.addCommitProject(project.getItem1(), dstCommit);
    Technique.addEdits(project.getItem1(), files, dstCommit);
    logger.trace("PROJECT: " + project.getItem1());
    final int currentCount = storage.getNumberEdits();
    logger.trace("NODE PROCESSED: " + currentCount);
    final String pname = project.getItem1();
    logger.trace("DEBUG COMMITS: " + storage.getCommitProjects().get(pname).size());
    logger.trace("DEBUG CURRENT COMMIT: " + dstCommit);
    return 1;
  }

  private static int indexOf(final String commit, final List<RevCommit> log) {
    for (int i = 0; i < log.size(); i++) {
      RevCommit rc = log.get(i);
      if (rc.getId().getName().equals(commit)) {
        return i;
      }
    }
    return -1;
  }
}
