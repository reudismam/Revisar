package br.ufcg.spg.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a tree.
 */
public class ATree<T> {
  /**
   * Parent tree.
   */
  private ATree<T> parent;
  
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
  
  /**
   * String value of tree.
   */
  private String strValue;
  
  /**
   * Private list of children.
   */
  private List<ATree<T>> children;

  public ATree(final T value) {
    this.value = value;
    children = new ArrayList<ATree<T>>();
  }

  public ATree<T> getParent() {
    return parent;
  }

  public void setParent(final ATree<T> parent) {
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

  public List<ATree<T>> getChildren() {
    return children;
  }

  public void setChildren(final List<ATree<T>> children) {
    this.children = children;
  }
  
  public void addChild(final ATree<T> child) {
    this.children.add(child);
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
      final ATree<T> child = children.get(i);
      treeSize += child.size();
    }
    return treeSize + 1;
  }
}
