package br.ufcg.spg.constraint.rule;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class Rule14 extends RuleBase {

  @Override
  public boolean isValidRule(final ASTNode node) {
    final CastExpression castExpr = (CastExpression) node;
    final ITypeBinding castExpType = castExpr.resolveTypeBinding();
    final ITypeBinding ttype = castExpr.getType().resolveBinding();
    return castExpType.getQualifiedName().equals(ttype.getQualifiedName());  
  }

  @Override
  public boolean isApplicableTo(final ASTNode node) {
    return node instanceof CastExpression;
  }
}
