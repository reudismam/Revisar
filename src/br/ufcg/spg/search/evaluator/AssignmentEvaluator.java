package br.ufcg.spg.search.evaluator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class AssignmentEvaluator implements IEvaluator {

  @Override
  public boolean evaluate(final ASTNode node) {
    if (!(node instanceof VariableDeclarationFragment)) {
      return false;
    }
    return true;
  }
}
