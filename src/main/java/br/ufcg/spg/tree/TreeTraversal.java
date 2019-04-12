package br.ufcg.spg.tree;

import java.util.ArrayList;
import java.util.List;

public class TreeTraversal<T> {
  public List<RevisarTree<T>> list;

  /**
   * Performs a post order traversal.
   */
  public List<RevisarTree<T>> postOrderTraversal(RevisarTree<T> t) {
    list = new ArrayList<RevisarTree<T>>();
    postOrder(t);
    return list;
  }

  private void postOrder(RevisarTree<T> t) {
    for (RevisarTree<T> ch : t.getChildren()) {
      postOrder(ch);
    }
    list.add(t);
  }
}
