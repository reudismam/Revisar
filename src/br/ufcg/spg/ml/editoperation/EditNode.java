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
}
