package br.ufcg.spg.ml.clustering;

import java.util.List;

import br.ufcg.spg.tree.RevisarTree;

public class StringTreeConverter {
  
  /**
   * Convert a RevisarTree to a string tree representation e.g., {a{b}{c}}.
   * @param revisarTree RevisarTree
   */
  public static String convertRevisasrTreeToString(RevisarTree<String> revisarTree) {
    List<RevisarTree<String>> list = revisarTree.getChildren();
    if (list.isEmpty()) {
      String content = revisarTree.getValue();
      String treeNode = "{" + content + "}";
      return treeNode;
    }
    String tree = "{" + revisarTree.getValue();
    for (RevisarTree<String> sot : revisarTree.getChildren()) {
      String node = convertRevisasrTreeToString(sot);
      tree += node;
    }
    tree += "}";
    return tree;
  }

}
