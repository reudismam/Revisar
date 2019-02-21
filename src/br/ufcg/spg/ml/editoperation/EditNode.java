package br.ufcg.spg.ml.editoperation;

import br.ufcg.spg.tree.RevisarTree;

public class EditNode<T> {

  private RevisarTree<T> parent;
  private RevisarTree<T> t1Node;
  private RevisarTree<T> previousParent;
  private int k;
  
  /**
   * Constructor.
   */
  public EditNode(RevisarTree<T> t1Node, 
      RevisarTree<T> parent, RevisarTree<T> previousParent, int k) {
    this.t1Node = t1Node;
    this.parent = parent;
    this.previousParent = previousParent;
    this.k = k;
  }
  
  public RevisarTree<T> getParent() {
    return parent;
  }
  
  public void setParent(RevisarTree<T> parent) {
    this.parent = parent;
  }
  
  public RevisarTree<T> getT1Node() {
    return t1Node;
  }

  public void setT1Node(RevisarTree<T> t1Node) {
    this.t1Node = t1Node;
  }

  public RevisarTree<T> getPreviousParent() {
    return previousParent;
  }
  
  public void setPreviousParent(RevisarTree<T> previousParent) {
    this.previousParent = previousParent;
  }

  public int getK() {
    return k;
  }

  public void setK(int k) {
    this.k = k;
  }
  
  /**
   * Equals.
   */
  protected boolean thisEquals(EditNode<T> other) {
    boolean isParentLabel = isParentLabel(other);
    return evaluateElements(other) && isParentLabel;
  }
  
  /**
   * Evaluates is parent same label.
   */
  protected boolean isParentLabel(EditNode<T> other) {
    boolean isParentLabel = false;
    if (getParent() != null && other.getParent() != null) {
      isParentLabel = other.getParent().getStrLabel().equals(getParent().getStrLabel());
    } else if (getParent() == null && other.getParent() == null) {
      isParentLabel = true;
    }
    return isParentLabel;
  }
  
  private boolean evaluateElements(EditNode<T> other) {
    return getK() == other.getK() 
        && other.getT1Node().getStrLabel().equals(getT1Node().getStrLabel());
  }
  
  /**
   * Formats label.
   */
  public String formatLabel(String label) {
    if (label.startsWith("hash")) {
      return "hash";
    }
    return label;
  }
  
  public String identity() {
	  String str =  formatLabel(getT1Node().getStrLabel());
	  if (getParent() != null) {
		  str = str + ", " + formatLabel(getParent().getStrLabel());
	  } else {
		  str = str + ", null";
	  }
	  return str;
  }
}
