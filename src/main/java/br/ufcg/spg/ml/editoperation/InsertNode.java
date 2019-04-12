package br.ufcg.spg.ml.editoperation;

import br.ufcg.spg.tree.RevisarTree;

public class InsertNode<T> extends EditNode<T> {

  /**
   * Constructor.
   */
  public InsertNode(RevisarTree<T> node, RevisarTree<T> parent, int k) {
    super(node, parent, null, k);
  }

  /**
   * String representation of this object.
   */
  @Override
  public String toString() {
    return "Insert(" + getT1Node().getStrLabel() + ", " 
        + getParent().getStrLabel() + ", " + getK() + ")";
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
  
  @Override
  public String identity() {
	  return "Insert(" + super.identity() + ")";
  }
}
