package br.ufcg.spg.converter;

import at.unisalzburg.dbresearch.apted.node.Node;

import br.ufcg.spg.tree.RevisarTree;
import java.util.List;

public final class ConverterHelper {
  
  private ConverterHelper() {
  }
  
  /**
   * Makes a copy of the given tree.
   */
  public static <T> RevisarTree<T> makeACopy(RevisarTree<T> st) {
    if (st == null) return null;
    List<RevisarTree<T>> list = st.getChildren();
    if (list.isEmpty()) {
      return new RevisarTree<>(st.getValue(), st.getLabel());
    }
    RevisarTree<T> tree = new RevisarTree<>(st.getValue(), st.getLabel());
    for (RevisarTree<T> sot : st.getChildren()) {
      RevisarTree<T> node = makeACopy(sot);
      tree.addChild(node);
    }
    return tree;
  }
  
  /**
   * convert node to RevisarTree.
   */
  public static <T> RevisarTree<T> convertNodeToRevisarTree(Node<T> st) {
    List<Node<T>> list = st.getChildren();
    if (list.isEmpty()) {
      return new RevisarTree<>(st.getNodeData(), st.toString());
    }
    RevisarTree<T> tree = new RevisarTree<>(st.getNodeData(), st.toString());
    for (Node<T> sot : st.getChildren()) {
      RevisarTree<T> node = convertNodeToRevisarTree(sot);
      tree.addChild(node);
    }
    return tree;
  }
}
