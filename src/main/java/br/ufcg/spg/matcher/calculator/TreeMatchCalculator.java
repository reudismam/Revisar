package br.ufcg.spg.matcher.calculator;

import br.ufcg.spg.matcher.IMatcher;

import com.github.gumtreediff.tree.ITree;

import java.util.List;

/**
 * Performs match operations.
 *
 */
public class TreeMatchCalculator extends MatchCalculator<ITree> {
  
  /**
   * Constructor.
   */
  public TreeMatchCalculator(IMatcher<ITree> evaluator) {
    super(evaluator);
  }
  
  /**
   * {@inheritDoc}
   */
  public List<ITree> chilren(ITree st) {
    return st.getChildren();
  }
}
