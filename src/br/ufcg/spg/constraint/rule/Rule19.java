package br.ufcg.spg.constraint.rule;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class Rule19 extends RuleBase {

  @Override
  public boolean isValidRule(final ASTNode node) {
    final ThisExpression thisExpr = (ThisExpression) node;
    final ITypeBinding thisBinding = thisExpr.resolveTypeBinding();
    final TypeDeclaration typeDcl = RuleUtils.getTypeDeclaration(node);
    final ITypeBinding typeBinding = typeDcl.resolveBinding();
    return thisBinding.getQualifiedName().equals(typeBinding.getQualifiedName());
  }
  
  @Override
  public boolean isApplicableTo(final ASTNode node) {
    return node instanceof ThisExpression;
  }
}
