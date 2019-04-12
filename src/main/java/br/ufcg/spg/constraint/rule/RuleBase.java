package br.ufcg.spg.constraint.rule;

import br.ufcg.spg.constraint.IConstraintRule;
import org.eclipse.jdt.core.dom.ASTNode;

public abstract class RuleBase implements IConstraintRule {

  @Override
  public boolean isValid(final ASTNode node) {
    if (!isApplicableTo(node)) {
      return false;
    }
    try {
      return isValidRule(node);
    } catch (final Exception e) {
      return false;
    }
  }
  
  abstract boolean isValidRule(ASTNode node);
}
