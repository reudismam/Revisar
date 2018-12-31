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
    List<RevisarTree<T>> children = new ArrayList<>();
    for (RevisarTree<T> sot : st.getChildren()) {
      RevisarTree<T> node = makeACopy(sot);
      node.setParent(st);
      children.add(node);
    }
    RevisarTree<T> tree = new RevisarTree<T>(st.getValue(), st.getLabel(), children);
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
    List<RevisarTree<T>> children = new ArrayList<>();
    RevisarTree<T> tree = new RevisarTree<T>(
        st.getNodeData(), st.toString(), children);
    for (Node<T> sot : st.getChildren()) {
      RevisarTree<T> node = convertNodeToRevisarTree(sot);
      node.setParent(tree);
      children.add(node);
    }
    return tree;
  }
}
