package br.ufcg.spg.tree;

import java.util.ArrayList;
import java.util.List;

import br.ufcg.spg.ml.editoperation.UpdateNode;

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

  /*public RevisarTree(final T value) {
    this.value = value;
    children = new ArrayList<RevisarTree<T>>();
  }*/
  
  /**
   * Constructor.
   */
  public RevisarTree(final T value, final Object label) {
    this.value = value;
    this.label = label;
    children = new ArrayList<RevisarTree<T>>();
  }

  /**
   * Constructor.
   */
  public RevisarTree(T value, Object label, List<RevisarTree<T>> children) {
    this.value = value;
    this.label = label;
    this.children = children;
    for (RevisarTree<T> child : children) {
      child.setParent(this);
    }
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
  
  /**
   * Gets the label.
   */
  public Object getLabel() {
    return label;
  }
  
  /**
   * Gets the string label.
   */
  public String getStrLabel() {
    String  labelSub = label.toString().substring(1);
    if (labelSub.startsWith("hash")) {
      return labelSub.substring(0, labelSub.length() - 1);
    } else if (labelSub.indexOf("{") != -1) {
      return labelSub.substring(0, labelSub.indexOf("{"));
    }
    if (label.toString().startsWith("{")) {
      return label.toString().substring(1, label.toString().length() - 1);
    }
    return label.toString();
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
  
  /**
   * Add a child at k position.
   * 
   * @param child
   *          Child
   * @param k
   *          position
   */
  public void addChild(RevisarTree<T> child, int k) {
    if (child == null) {
      throw new RuntimeException("child could not be null.");
    }
    child.setParent(this);
    children.add(k, child);
  }
  
  public void addChild(final RevisarTree<T> child) {
    child.setParent(this);
    this.children.add(child);
  }
  
  public void removeChild(int k) {
    this.children.remove(k);
  }
  
  public String getStrValue() {
    return strValue;
  }

  public void setStrValue(final String strValue) {
    this.strValue = strValue;
  }
  
  /**
   * Computer size.
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
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "ATree : " + value;
  }
  
  /**
   * {@inheritDoc}.
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof RevisarTree<?>)) {
      return false;
    }
    @SuppressWarnings("unchecked")
    RevisarTree<T> compare = (RevisarTree<T>) obj;
    if (compare.getLabel().equals("root") && getLabel().equals("root")) {
      return true;
    } else if (compare.getLabel().equals("root") || getLabel().equals("root")) {
      return false;
    }
    return value.equals(compare.getValue());
  }
  
  public boolean isRoot() {
    return getLabel().equals("root");
  }

}
