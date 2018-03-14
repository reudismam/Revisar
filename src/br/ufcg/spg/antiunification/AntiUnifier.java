package br.ufcg.spg.antiunification;

import at.jku.risc.stout.urauc.algo.AntiUnifyProblem.VariableWithHedges;

import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeParser;
import br.ufcg.spg.util.PrintUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that performs anti-unification.
 *
 */
public class AntiUnifier {
  /**
   * Left-hand side anti-unification.
   */
  private AntiUnifier left;
  
  /**
   * Mid anti-unification.
   */
  private AntiUnifier mid;
  
  /**
   * Right-hand side anti-unification.
   */
  private AntiUnifier right;
  
  /**
   * Anti-unification value.
   */
  private AntiUnificationData value;
  
  /**
   * Parent for this anti-unification.
   */
  private AntiUnifier parent;

  /**
   * Default constructor.
   */
  public AntiUnifier() {
  }

  /**
   * Constructor.
   * @param value anti-unification value
   */
  public AntiUnifier(final AntiUnificationData value) {
    this.value = value;
  }

  /**
   * constructor.
   * @param value anti-unification value
   */
  public AntiUnifier(final String value) {
    final AntiUnificationData newAu = new AntiUnificationData(
        value, new ArrayList<VariableWithHedges>());
    this.value = newAu;
  }

  /**
   * Adds one child to the anti-unification.
   * @param auLeft left-hand side
   * @param mid middle
   * @param auRight right-hand side
   */
  public void addChildren(final AntiUnifier auLeft, final AntiUnifier mid, 
      final AntiUnifier auRight) {
    this.left = auLeft;
    this.mid = mid;
    this.right = auRight;
    this.left.setParent(this);
    this.mid.setParent(this);
    this.left.setParent(this);
  }

  /**
   * Gets the right-hand side of the anti-unification.
   * @return the right
   */
  public AntiUnifier getRight() {
    return right;
  }

  /**
   * Sets the right-hand side of the anti-unification.
   * @param right
   *          the right to set
   */
  public void setRight(final AntiUnifier right) {
    this.right = right;
  }

  /**
   * Gets the left side of the anti-unification.
   * @return the left
   */
  public AntiUnifier getLeft() {
    return left;
  }

  /**
   * Gets the left-hand side of the anti-unification.
   * @param left
   *          the left to set
   */
  public void setLeft(final AntiUnifier left) {
    this.left = left;
  }

  /**
   * Gets the value of the anti-unification.
   * @return the value
   */
  public AntiUnificationData getValue() {
    return value;
  }

  /**
   * Sets the value of the anti-unification.
   * @param value
   *          the value to set
   */
  public void setValue(final AntiUnificationData value) {
    this.value = value;
  }

  /**
   * Gets the middle of the anti-unification.
   * @return the mid
   */
  public AntiUnifier getMid() {
    return mid;
  }

  /**
   * @param mid
   *          the mid to set.
   */
  public void setMid(final AntiUnifier mid) {
    this.mid = mid;
  }

  /**
   * Gets the parent of the anti-unification
   * @return the parent.
   */
  public AntiUnifier getParent() {
    return parent;
  }

  /**
   * @param parent
   *          the parent to set.
   */
  public void setParent(final AntiUnifier parent) {
    this.parent = parent;
  }

  /**
   * Gets children nodes.
   * 
   * @return children nodes
   */
  public List<AntiUnifier> getChildren() {
    final List<AntiUnifier> children = new ArrayList<AntiUnifier>();
    if (left != null && left.getValue() != null) {
      children.add(left);
    }
    if (mid != null && mid.getValue() != null) {
      children.add(mid);
    }
    if (right != null && right.getValue() != null) {
      children.add(right);
    }
    return children;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    final RevisarTree<String> atree = toATree();
    final String output = PrintUtils.prettyPrint(atree);
    return output;
  }

  /**
   * Converts a tree to an string.
   * @return string representation of the tree.
   */
  public String toStringTree() {
    String output = "[ " + this.getValue() + " ]";
    boolean printLeft = false;
    boolean printMid = false;
    if (left != null && left.getValue() != null) {
      output += " [ " + left.toString() + " ]";
      printLeft = true;
    }
    if (mid != null) {
      if (printLeft) {
        output = ",  ";
      }
      output += "[ " + mid.toString() + " ]";
      printMid = true;
    }
    if (right != null && right.getValue() != null) {
      if (printLeft || printMid) {
        output += ", ";
      }
      output += "[ " + right.toString() + "]";
    }
    return output + "\n\n";
  }

  /**
   * Converts this ant-unification to a tree.
   * @return the tree version of the ant-unification
   */
  public RevisarTree<String> toATree() {
    final RevisarTree<String> tree = RevisarTreeParser.parser(this.getValue().getUnifier());
    if (left != null && left.getValue() != null) {
      tree.getChildren().add(left.toATree());
    }
    if (mid != null) {
      tree.getChildren().add(mid.toATree());
    }
    if (right != null && right.getValue() != null) {
      tree.getChildren().add(right.toATree());
    }
    return tree;
  }
}
