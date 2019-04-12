package br.ufcg.spg.constraint.rule;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;

public class Rule18 extends RuleBase {

  @Override
  public boolean isValidRule(final ASTNode node) {
    final Type type = (Type) node;
    final ITypeBinding nullType = node.getAST().resolveWellKnownType("null");
    final ITypeBinding typeBinding = type.resolveBinding();
    if (nullType != null) {
      return nullType.isSubTypeCompatible(typeBinding); 
    }
    return true;
  }

  @Override
  public boolean isApplicableTo(final ASTNode node) {
    return node instanceof Type;
  }
}
