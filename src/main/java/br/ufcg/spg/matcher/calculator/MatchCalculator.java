package br.ufcg.spg.matcher.calculator;

import br.ufcg.spg.matcher.IMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs match operations.
 *
 */
public abstract class MatchCalculator<T> {

  /**
   * List of nodes in the AST.
   */
  private transient List<T> nodes;

  /**
   * List of indexes for each node.
   */
  private transient List<Integer> indexes;
  
  public IMatcher<T> evaluator;

  /**
   * Current index.
   */
  protected transient int currentIndex;

  /**
   * Constructor.
   */
  public MatchCalculator(IMatcher<T> evaluator) {
    this.evaluator = evaluator;
  }

  /**
   * Returns node at given start and position.
   * 
   * @param root
   *          root node
   */
  public void fillNode(final T root) {
    currentIndex = 0;
    nodes = new ArrayList<>();
    indexes = new ArrayList<>();
    buildNodesFromRoot(root);
  }

  /**
   * Gets node.
   * 
   * @param root
   *          root node
   * @return node in root
   */
  public T getNode(final T root) {
    fillNode(root);
    if (nodes.isEmpty()) {
      return null;
    }
    return nodes.get(0);
  }
  
  /**
   * Gets node.
   * 
   * @param root
   *          root node
   * @return node in root
   */
  public List<T> getNodes(final T root) {
    fillNode(root);
    return nodes;
  }

  /**
   * Gets index.
   * 
   * @param root
   *          root node
   * @return index
   */
  public int getIndex(final T root) {
    fillNode(root);
    if (indexes.isEmpty()) {
      return -1;
    }
    return indexes.get(0);
  }

  /**
   * Returns nodes for a position.
   * 
   * @param obj
   *          current object
   * @param start
   *          time
   * @param end
   *          time
   */
  private void buildNodesFromRoot(final T st) {
    currentIndex++;
    if (evaluator.evaluate(st)) {
      nodes.add(st);
      indexes.add(currentIndex);
    }
    final List<T> children = chilren(st);
    for (int i = 0; i < children.size(); i++) {
      final T sot = children.get(i);
      buildNodesFromRoot(sot);
    }
  }

  protected abstract List<T> chilren(T st);
}
