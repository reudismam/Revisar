package br.ufcg.spg.search.evaluator;

import org.eclipse.jdt.core.dom.ASTNode;

public class SizeEvaluator implements IEvaluator {
  
  public SizeEvaluator() {
  }

  @Override
  public boolean evaluate(final ASTNode node) {
    return node.getLength() >= 100;
  }
}
