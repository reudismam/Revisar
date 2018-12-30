package br.ufcg.spg.converter;

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
      children.add(node);
    }
    RevisarTree<T> tree = new RevisarTree<T>(st.getValue(), st.getLabel(), children);
    return tree;
  }
}
