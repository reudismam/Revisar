package br.ufcg.spg.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a tree.
 */
public class RevisarTree<T> {
  /**
   * Parent tree.
   */
  private RevisarTree<T> parent;
  
  /**
   * Start position.
   */
  private int pos;
  
  /**
   * End position.
   */
  private int end;
  
  /**
   * Tree value.
   */
  private T value;
  
  private Object label;
  
  /**
   * String value of tree.
   */
  private String strValue;
  
  /**
   * Private list of children.
   */
  private List<RevisarTree<T>> children;

  public RevisarTree(final T value) {
    this.value = value;
    children = new ArrayList<RevisarTree<T>>();
  }
  
  public RevisarTree(final T value, final Object label) {
    this.value = value;
    this.label = label;
  }

  public RevisarTree(T value, Object label, List<RevisarTree<T>> children) {
    this.value = value;
    this.label = label;
    this.children = children;
  }

  public RevisarTree<T> getParent() {
    return parent;
  }

  public void setParent(final RevisarTree<T> parent) {
    this.parent = parent;
  }
  
  public int getPos() {
    return pos;
  }

  public void setPos(final int pos) {
    this.pos = pos;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(final int end) {
    this.end = end;
  }

  public T getValue() {
    return value;
  }

  public void setValue(final T value) {
    this.value = value;
  }
  
  public Object getLabel() {
    return label;
  }

  public void setLabel(Object label) {
    this.label = label;
  }

  public List<RevisarTree<T>> getChildren() {
    return children;
  }

  public void setChildren(final List<RevisarTree<T>> children) {
    this.children = children;
  }
  
  public void addChild(final RevisarTree<T> child) {
    this.children.add(child);
    child.setParent(this);
  }
  
  public String getStrValue() {
    return strValue;
  }

  public void setStrValue(final String strValue) {
    this.strValue = strValue;
  }

  @Override
  public String toString() {
    return "ATree : " + value;
  }
  
  /**
   * Converts a ASTNode to an anti-unification equation.
   */
  public int size() {
    if (children.isEmpty()) {
      return 1;
    }
    int treeSize = 0;
    for (int i = 0; i < children.size(); i++) {
      final RevisarTree<T> child = children.get(i);
      treeSize += child.size();
    }
    return treeSize + 1;
  }
}
