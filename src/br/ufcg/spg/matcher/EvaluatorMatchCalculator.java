package br.ufcg.spg.matcher;

import br.ufcg.spg.search.evaluator.IEvaluator;
import com.github.gumtreediff.tree.ITree;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Match based on an evaluator.
 */
public class EvaluatorMatchCalculator extends AbstractMatchCalculator {
  
  /**
   * Evaluator.
   */
  private final transient IEvaluator evaluator;
  
  /**
   * Constructor.
   * @param evaluator evaluator
   */
  public EvaluatorMatchCalculator(final IEvaluator evaluator) {
    super();
    this.evaluator = evaluator;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean evaluate(final ITree itree) {
    throw new UnsupportedOperationException("Could not apply for evaluator.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean evaluate(final ASTNode astNode) {
    return evaluator.evaluate(astNode);
  }

}
