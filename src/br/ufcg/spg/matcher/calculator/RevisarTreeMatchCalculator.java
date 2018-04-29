package br.ufcg.spg.matcher.calculator;

import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.tree.RevisarTree;

import java.util.List;

/**
 * Performs match operations.
 *
 */
public class RevisarTreeMatchCalculator<T> extends MatchCalculator<RevisarTree<T>> {
  
  /**
   * Constructor.
   */
  public RevisarTreeMatchCalculator(IMatcher<RevisarTree<T>> evaluator) {
    super(evaluator);
  }
  
  /**
   * {@inheritDoc}
   */
  public List<RevisarTree<T>> chilren(RevisarTree<T> st) {
    final List<RevisarTree<T>> children = st.getChildren();
    return children;
  }
}
