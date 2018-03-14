package br.ufcg.spg.technique;

import at.jku.risc.stout.urauc.algo.JustificationException;
import at.jku.risc.stout.urauc.util.ControlledException;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.config.TechniqueConfig;
import br.ufcg.spg.edit.EditStorage;
import br.ufcg.spg.exp.ExpUtils;
import br.ufcg.spg.git.GitUtils;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

public class TechniqueUtils {
  
  /**
   * Extract concrete edits.
   * @param project project
   */
  public static void concreteEdits(final Tuple<String, String> project)
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
      System.out.print("NODE PROCESSED:");
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
}
