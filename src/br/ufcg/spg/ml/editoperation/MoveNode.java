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
    return "Move(" + getT1Node().getLabel() + " to " + getParent().getLabel() + ", " + getK() + ")";
  }
  
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MoveNode<?>)) {
      return false;
    }
    return thisEquals((MoveNode<T>) obj);
  }

  private boolean thisEquals(MoveNode<T> other) {
    boolean isParentLabel = false;
    if (getParent() != null && other.getParent() != null) {
      isParentLabel = other.getParent().getLabel().equals(getParent().getLabel());
    } else if (getParent() == null && other.getParent() == null) {
      isParentLabel = true;
    }
    return getK() == other.getK() 
        && other.getT1Node().getLabel().equals(getT1Node().getLabel()) 
        && isParentLabel;
  }
}
