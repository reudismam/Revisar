package br.ufcg.spg.ml.traversal;

import at.unisalzburg.dbresearch.apted.node.Node;
import at.unisalzburg.dbresearch.apted.node.StringNodeData;

import java.util.ArrayList;
import java.util.List;

public class PostOrderTraversal {
  public List<Node<StringNodeData>> list;

  /**
   * Performs a post-order traversal.
   */
  public List<Node<StringNodeData>> postOrderTraversal(Node<StringNodeData> t) {
    list = new ArrayList<>();
    postOrder(t);
    return list;
  }

  private void postOrder(Node<StringNodeData> t) {
    for (Node<StringNodeData> ch : t.getChildren()) {
      postOrder(ch);
    }
    list.add(t);
  }
}
