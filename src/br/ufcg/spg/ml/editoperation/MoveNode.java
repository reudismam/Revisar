package br.ufcg.spg.ml.editoperation;

import br.ufcg.spg.tree.RevisarTree;

public class MoveNode<T> extends EditNode<T> {
  public MoveNode(RevisarTree<T> parent, RevisarTree<T> node, int k) {
    super(node, parent, null, k);
  }

  /**
   * String represent on this object.
   */
  @Override
  public String toString() {
    return "Move(" + getT1Node().getStrLabel() + " to " 
        + getParent().getStrLabel() + ", " + getK() + ")";
  }
  
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MoveNode<?>)) {
      return false;
    }
    return thisEquals((MoveNode<T>) obj);
  }
}
