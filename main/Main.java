package br.ufcg.spg.main;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.exp.ExpUtils;
import br.ufcg.spg.main.command.ExtractConcreteEditsCommand;
import br.ufcg.spg.main.command.ICommand;
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
    if (args[0].equals("-e")) {
      final List<Tuple<String, String>> projects = ExpUtils.getProjects();
      final ICommand command = new ExtractConcreteEditsCommand(projects);
      command.execute();
    }
  }
}
