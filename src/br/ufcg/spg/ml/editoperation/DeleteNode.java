package br.ufcg.spg.ml.editoperation;

import br.ufcg.spg.tree.RevisarTree;

public class DeleteNode<T> extends EditNode<T> {

  /**
   * Constructor.
   */
  public DeleteNode(RevisarTree<T> node, RevisarTree<T> parent) {
    super(node, parent, null, -1);
    if (getParent() == null) {
      parent = new RevisarTree<T>(null, "root");
      parent.addChild(getT1Node());
      setParent(parent);
    }
  }

  /**
   * Constructor.
   */
  public DeleteNode(RevisarTree<T> node) {
    super(node, node.getParent(), null, -1);
    if (getParent() == null) {
      RevisarTree<T> parent = new RevisarTree<T>(null, "root");
      parent.addChild(getT1Node());
      setParent(parent);
    }
  }

  @Override
  public String toString() {
    return "Delete(" + getT1Node() + ")";
  }
  
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DeleteNode<?>)) {
      return false;
    }
    return thisEquals((DeleteNode<T>) obj);
  }

  private boolean thisEquals(DeleteNode<T> other) {
    return this.getK() == other.getK() 
        && other.getT1Node().getStrLabel().equals(this.getT1Node().getStrLabel()) 
        && other.getParent().getStrLabel().equals(this.getParent().getStrLabel());
  }
}
