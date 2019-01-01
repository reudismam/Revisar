package br.ufcg.spg.converter;

import at.unisalzburg.dbresearch.apted.node.Node;

import br.ufcg.spg.tree.RevisarTree;
import java.util.ArrayList;
import java.util.List;

public class ConverterHelper {
  /**
   * Makes a copy of the given tree.
   */
  public static <T> RevisarTree<T> makeACopy(RevisarTree<T> st) {
    List<RevisarTree<T>> list = st.getChildren();
    if (list.isEmpty()) {
      return new RevisarTree<T>(st.getValue(), st.getLabel());
    }
    RevisarTree<T> tree = new RevisarTree<T>(st.getValue(), st.getLabel());
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
      return new RevisarTree<T>(st.getNodeData(), st.toString());
    }
    RevisarTree<T> tree = new RevisarTree<T>(st.getNodeData(), st.toString());
    for (Node<T> sot : st.getChildren()) {
      RevisarTree<T> node = convertNodeToRevisarTree(sot);
      tree.addChild(node);
    }
    return tree;
  }
}
