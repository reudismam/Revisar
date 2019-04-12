package br.ufcg.spg.constraint.rule;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;

public class Rule17 extends RuleBase {

  @Override
  public boolean isValidRule(final ASTNode node) {
    final Type type = (Type) node;
    final ITypeBinding objType = node.getAST().resolveWellKnownType("java.lang.Object");
    final ITypeBinding typeBinding = type.resolveBinding();
    //In case, typeBinding is null.
    if (typeBinding == null) {
      return false;
    }
    return typeBinding.isSubTypeCompatible(objType);
  }

  @Override
  public boolean isApplicableTo(final ASTNode node) {
    return node instanceof Type;
  }
}
