package br.ufcg.spg.main;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.exp.ExpUtils;
import br.ufcg.spg.main.command.ExtractConcreteEditsCommand;
import br.ufcg.spg.main.command.ICommand;
import br.ufcg.spg.string.StringUtils;
import br.ufcg.spg.technique.Technique;

import java.util.List;

/**
 * Main class.
 */
public class Main {

  /**
   * Main program.
   * @param args arguments
   */
  public static void main(final String[] args) {
    if (args.length == 0) {
      menu();
      return;
    }
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.equals("-e")) {
        if (args.length <= i + 2) {
          System.out.println("Error: please, specify a project and project folder.");
          menu();
          return;
        } else {
          String projects = args[i + 1];
          String projectFolder = args[i + 2];
          MainArguments.getInstance().setProjects(projects);
          MainArguments.getInstance().setProjectFolder(projectFolder);
        }
        final List<Tuple<String, String>> projects = ExpUtils.getProjects();
        final ICommand command = new ExtractConcreteEditsCommand(projects);
        command.execute();
      }
      if (arg.equals("-c")) {
        System.out.println("CLUSTERING EDITS.");
        Technique.clusterEdits();
      }
      if (arg.equals("-t")) {
        System.out.println("LEARNING TRANSFORATIONS");
        Technique.translateEdits();
      }
      if (arg.equals("-tid")) {
        if (args.length <= i + 1) {
          System.out.println("Error, please, specify the id of the cluster");
          menu();
          return;
        } else {
          String clusterId = args[i + 1];
          Technique.translateEdits(clusterId);
        }
      }
    }
  }
  
  /**
   * Shows option menu.
   */
  public static void menu() {
    System.out.println("Usage: revisar.jar [-options] [args...]");
    System.out.println("where options include:");
    System.out.println("\t-e:<projects> <projectFolder>   to extract concrete edits");
    System.out.println("\t-c                              to cluster concrete edits");
    System.out.println("\t-t                              to learn transformations");
    System.out.println("\t-tid:<cluster_id>               "
        + "to learn a transformation for a specific cluster");
  }
}
