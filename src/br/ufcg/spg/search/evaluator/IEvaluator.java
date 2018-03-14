package br.ufcg.spg.search.evaluator;

import org.eclipse.jdt.core.dom.ASTNode;

public interface IEvaluator {
  public boolean evaluate(ASTNode node);
}
