package br.ufcg.spg.search.evaluator;

import org.eclipse.jdt.core.dom.ASTNode;

public class AllwaysTrueEvaluator implements IEvaluator {
  
  public AllwaysTrueEvaluator() {
  }

  @Override
  public boolean evaluate(final ASTNode node) {
    return true;
  }
}
