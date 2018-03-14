package br.ufcg.spg.matcher.calculator;

import com.github.gumtreediff.tree.ITree;

import br.ufcg.spg.matcher.IMatcher;

import java.util.List;

/**
 * Performs match operations.
 *
 */
public class TreeMatchCalculator extends AbstractMatchCalculator<ITree> {
  
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
    final List<ITree> children = st.getChildren();
    return children;
  }
}
