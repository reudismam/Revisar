package br.ufcg.spg.ml.traversal;

import at.unisalzburg.dbresearch.apted.node.Node;
import at.unisalzburg.dbresearch.apted.node.StringNodeData;

import java.util.ArrayList;
import java.util.List;

public class PostOrderTraversal {
  public List<Node<StringNodeData>> list;

  public List<Node<StringNodeData>> postOrderTraversal(Node<StringNodeData> t) {
    list = new ArrayList<Node<StringNodeData>>();
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
