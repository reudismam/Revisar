package br.ufcg.spg.search.evaluator;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public class CompositeEvaluator implements IEvaluator {
  private final List<IEvaluator> evaluators;
  
  public CompositeEvaluator(final List<IEvaluator> evaluators) {
    this.evaluators = evaluators;
  }

  @Override
  public boolean evaluate(final ASTNode node) {
    for (final IEvaluator evaluator : evaluators) {
      if (evaluator.evaluate(node)) {
        return true;
      }
    }
    return false;
  }
}
