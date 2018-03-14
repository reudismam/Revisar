package br.ufcg.spg.main.command;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.edit.EditStorage;
import br.ufcg.spg.log.TimeLogger;
import br.ufcg.spg.technique.TechniqueUtils;

import java.util.List;

public class ExtractConcreteEditsCommand implements ICommand {
  private List<Tuple<String, String>> projects;
  
  public ExtractConcreteEditsCommand() {
  }

  public ExtractConcreteEditsCommand(final List<Tuple<String, String>> projects) {
    super();
    this.projects = projects;
  }

  public List<Tuple<String, String>> getProjects() {
    return projects;
  }

  public void setProjects(final List<Tuple<String, String>> projects) {
    this.projects = projects;
  }

  @Override
  public void execute() {
    try {
      final long startTime = System.nanoTime();     
      for (final Tuple<String, String> project : projects) {
        TechniqueUtils.concreteEdits(project);
      }
      final long estimatedTime = System.nanoTime() - startTime;
      TimeLogger.getInstance().setTimeExtract(estimatedTime);
      System.out.println("DEBUG: TOTAL COMMITS");
      final EditStorage storage = EditStorage.getInstance();
      for (final Tuple<String, String> project: projects) {
        System.out.println("=====================");
        System.out.println(project.getItem1());
        System.out.println("Total: " + storage.getCommitProjects().get(project.getItem1()).size());
        System.out.println("=====================");     
      }
      System.out.println("END.");
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
