package br.ufcg.spg.ml.editoperation;

import br.ufcg.spg.tree.RevisarTree;

public class UpdateNode<T> extends EditNode<T> {
  private RevisarTree<T> to;
  private RevisarTree<T> toParent;

  public UpdateNode(RevisarTree<T> from, RevisarTree<T> to, 
      RevisarTree<T> parent) {
    super(from, parent, null, -1);
    this.to = to;
  }
  
  /**
   * Constructor.
   */
  public UpdateNode(RevisarTree<T> from, RevisarTree<T> to, 
      RevisarTree<T> parent, RevisarTree<T> toParent) {
    super(from, parent, null, -1);
    this.to = to;
    this.toParent = toParent;
  }

  public RevisarTree<T> getTo() {
    return to;
  }

  public void setTo(RevisarTree<T> to) {
    this.to = to;
  }
  
  public RevisarTree<T> getToParent() {
    return toParent;
  }

  public void setToParent(RevisarTree<T> toParent) {
    this.toParent = toParent;
  }

  /**
   * String representation of this object.
   */
  @Override
  public String toString() {
    return "Update(" + getT1Node().getStrLabel() + " to " + to.getStrLabel() + ")";
  }
  
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof UpdateNode<?>)) {
      return false;
    }
    return thisEquals((UpdateNode<T>) obj);
  }

  private boolean thisEquals(UpdateNode<T> other) {
    boolean isParentLabel = isParentLabel(other);
    return formatLabel(other.getT1Node().getStrLabel())
        .equals(formatLabel(getT1Node().getStrLabel()))
        && formatLabel(other.getTo().getStrLabel())
        .equals(formatLabel(getTo().getStrLabel())) 
        && isParentLabel;
  }
}
