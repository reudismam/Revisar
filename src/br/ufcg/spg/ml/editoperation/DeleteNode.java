package br.ufcg.spg.ml.editoperation;

import br.ufcg.spg.tree.RevisarTree;

public class DeleteNode<T> extends EditNode<T> {

  public DeleteNode(RevisarTree<T> node, RevisarTree<T> parent) {
    super(node, parent, null, -1);
  }

  public DeleteNode(RevisarTree<T> node) {
    super(node, node.getParent(), null, -1);
  }

  @Override
  public String toString() {
    return "Delete(" + getT1Node() + ")";
  }
  
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DeleteNode<?>)) {
      return false;
    }
    return thisEquals((DeleteNode<T>) obj);
  }

  private boolean thisEquals(DeleteNode<T> other) {
    return this.getK() == other.getK() 
        && other.getT1Node().getLabel().equals(this.getT1Node().getLabel()) 
        && other.getParent().getLabel().equals(this.getParent().getLabel());
  }
}
