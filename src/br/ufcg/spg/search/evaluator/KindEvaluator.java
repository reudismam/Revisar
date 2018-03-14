package br.ufcg.spg.search.evaluator;

import org.eclipse.jdt.core.dom.ASTNode;

public class KindEvaluator implements IEvaluator {
  
  private final int kind;
  
  public KindEvaluator(final int kind) {
    this.kind = kind;
  }

  @Override
  public boolean evaluate(final ASTNode node) {
    final int nodekind = node.getNodeType();
    return nodekind == kind;
  }
}
