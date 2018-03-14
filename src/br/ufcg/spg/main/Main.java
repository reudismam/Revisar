package br.ufcg.spg.main;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.exp.ExpUtils;
import br.ufcg.spg.main.command.ExtractConcreteEditsCommand;
import br.ufcg.spg.main.command.ICommand;
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
        if (args.length <= i + 1) {
          System.out.println("Error: please, specify a project.");
        } else {
          String projects = args[i + 1];
          MainArguments.getInstance().setProjects(projects);
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
    System.out.println("\t-e:<projects>         to select concrete edits");
        /*    -d64          use a 64-bit data model if available
            -server       to select the "server" VM
                          The default VM is server.

            -cp <class search path of directories and zip/jar files>
            -classpath <class search path of directories and zip/jar files>
                          A ; separated list of directories, JAR archives,
                          and ZIP archives to search for class files.
            -D<name>=<value>
                          set a system property
            -verbose:[class|gc|jni]
                          enable verbose output
            -version      print product version and exit
            -version:<value>
                          Warning: this feature is deprecated and will be removed
                          in a future release.
                          require the specified version to run
            -showversion  print product version and continue
            -jre-restrict-search | -no-jre-restrict-search
                          Warning: this feature is deprecated and will be removed
                          in a future release.
                          include/exclude user private JREs in the version search
            -? -help      print this help message
            -X            print help on non-standard options
            -ea[:<packagename>...|:<classname>]
            -enableassertions[:<packagename>...|:<classname>]
                          enable assertions with specified granularity
            -da[:<packagename>...|:<classname>]
            -disableassertions[:<packagename>...|:<classname>]
                          disable assertions with specified granularity
            -esa | -enablesystemassertions
                          enable system assertions
            -dsa | -disablesystemassertions
                          disable system assertions
            -agentlib:<libname>[=<options>]
                          load native agent library <libname>, e.g. -agentlib:hprof
                          see also, -agentlib:jdwp=help and -agentlib:hprof=help
            -agentpath:<pathname>[=<options>]
                          load native agent library by full pathname
            -javaagent:<jarpath>[=<options>]
                          load Java programming language agent, see java.lang.instrument
            -splash:<imagepath>
                          show splash screen with specified image*/
  }
}
