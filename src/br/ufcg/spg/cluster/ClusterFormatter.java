package br.ufcg.spg.cluster;

import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.string.StringUtils;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeParser;
import br.ufcg.spg.util.PrintUtils;

import java.util.List;

/**
 * Format cluster data.
 */
public final class ClusterFormatter {
  
  private static ClusterFormatter instance;
  
  /**
   * Header.
   */
  private String header;
  /**
   * Footer.
   */
  private String footer;
  
  private ClusterFormatter() {
    configtHeader();
    configFooter();
  }
  
  /**
   * Get a new ClusterFormatter instance.
   */
  public static ClusterFormatter getInstance() {
    if (instance == null) {
      instance = new ClusterFormatter();
    }
    return instance;
  }

  
  private void configtHeader() {
    StringBuilder content = new StringBuilder();
    content.append("============================================="
        + "===================================\n");
    content.append("=================================CLUSTER DATA"
        + "===================================\n");
    content.append("=============================================="
        + "==================================\n");
    header = content.toString();
  }
  
  /**
   * Format cluster header.
   */
  public String formatHeader() {
    return header;
  }

  
  private void configFooter() {
    StringBuilder content = new StringBuilder();
    content.append("================================================="
        + "===============================\n");
    content.append("==============================END OF CLUSTER DATA"
        + "===============================\n");
    content.append("================================================="
        + "===============================\n\n");
    footer = content.toString();
  }
  
  /**
   * Format cluster footer.
   */
  public String formatFooter() {
    return footer;
  }

  /**
   * Format cluster content.
   * @param clusteri srcCluster
   * @param clusterj dstCluster
   */
  public String formatClusterContent(final Cluster clusteri, final Cluster clusterj) {
    StringBuilder content = new StringBuilder();
    content.append(clusteri.getAu()).append("\n\n");
    content.append(clusterj.getAu()).append("\n\n");
    RevisarTree<String> tempBefore = RevisarTreeParser.parser(clusteri.getAu());
    String before = PrintUtils.prettyPrint(tempBefore);
    RevisarTree<String> tempAfter = RevisarTreeParser.parser(clusterj.getAu());
    String after = PrintUtils.prettyPrint(tempAfter);
    String addToLines = "          ";
    String afterFormated = formatOutput(after, addToLines);
    String output = StringUtils.printStringSideBySide(before, afterFormated);
    content.append(output);
    content.append(formatStringNodes(clusteri.getNodes()));
    return content.toString();
  }

  /**
   * Format output.
   * @param pattern pattern
   */
  public String formatOutput(final String pattern, final String addToLines) {
    StringBuilder newPattern = new StringBuilder();
    String addToMid = "    >>    ";
    String[] lines = pattern.split("\n");
    int mid = lines.length / 2;
    for (int i = 0; i < lines.length; i++) {
      if (i == mid) {
        newPattern.append(addToMid).append(lines[i]).append('\n');
      } else {
        newPattern.append(addToLines).append(lines[i]).append('\n');
      }
    }
    return newPattern.toString();
  }

  /**
   * Format list of nodes.
   */
  public String formatStringNodes(final List<Edit> srcNodes) {
    StringBuilder result = new StringBuilder();
    result.append("\nEXAMPLES IN THIS CLUSTER ").append(srcNodes.size()).append(":\n\n");
    StringBuilder beforeNodes = new StringBuilder();
    StringBuilder afterNodes = new StringBuilder();
    int count = 0;
    for (final Edit node : srcNodes) {
      beforeNodes.append(node.getText()).append('\n');
      afterNodes.append(node.getDst().getText()).append('\n');
      if (++count == 4) {
        break;
      }
    }
    String addToLines = "    >>    ";
    String afterOutput = formatOutput(afterNodes.toString(), addToLines);
    String output = StringUtils.printStringSideBySide(beforeNodes.toString(), afterOutput);
    result.append(output);
    result.append("...\n");
    return result.toString();
  }

  /**
   * Format cluster.
   */
  public StringBuilder formatCluster(final Cluster clusteri, 
      final Cluster clusterj, final String refaster) {
    StringBuilder content = new StringBuilder("");
    content.append(formatHeader());
    content.append("Cluster ID: " + clusteri.getId()).append('\n');
    content.append(refaster).append('\n');
    content.append(formatClusterContent(clusteri, clusterj));
    content.append(formatFooter());
    return content;
  }
}
