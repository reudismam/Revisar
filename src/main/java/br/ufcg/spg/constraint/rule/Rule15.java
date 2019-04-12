package br.ufcg.spg.constraint.rule;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Down cast.
 *
 */
public class Rule15 extends RuleBase {

  @Override
  public boolean isValidRule(final ASTNode node) {
    final CastExpression castExpr = (CastExpression) node;
    final ITypeBinding castExpType = castExpr.resolveTypeBinding();
    final ITypeBinding etype = castExpr.getExpression().resolveTypeBinding();
    return castExpType.isSubTypeCompatible(etype);
  }

  @Override
  public boolean isApplicableTo(final ASTNode node) {
    return node instanceof CastExpression;
  }
}
