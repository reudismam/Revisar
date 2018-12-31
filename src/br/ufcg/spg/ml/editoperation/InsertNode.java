package br.ufcg.spg.ml.editoperation;

import br.ufcg.spg.tree.RevisarTree;

public class InsertNode<T> extends EditNode<T> {

  /**
   * Constructor.
   */
  public InsertNode(RevisarTree<T> parent, RevisarTree<T> node, int k) {
    super(node, parent, null, k);
  }

  /**
   * String representation of this object.
   */
  @Override
  public String toString() {
    return "Insert(" + getT1Node().getLabel() + ", " + getParent().getLabel() + ", " + getK() + ")";
  }
  
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof InsertNode<?>)) {
      return false;
    }
    return thisEquals((InsertNode<T>) obj);
  }

  /**
   * Equals.
   */
  public boolean thisEquals(InsertNode<T> other) {
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
