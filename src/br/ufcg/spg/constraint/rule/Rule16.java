package br.ufcg.spg.constraint.rule;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Upper cast.
 *
 */
public class Rule16 extends RuleBase {

  @Override
  public boolean isValidRule(final ASTNode node) {
    final CastExpression castExpr = (CastExpression) node;
    final ITypeBinding castExpType = castExpr.resolveTypeBinding();
    final ITypeBinding etype = castExpr.getExpression().resolveTypeBinding();
    if (etype == null || castExpType == null) {
      return false;
    }
    return etype.isSubTypeCompatible(castExpType);
  }

  @Override
  public boolean isApplicableTo(final ASTNode node) {
    return node instanceof CastExpression;
  }
}
