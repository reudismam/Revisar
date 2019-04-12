package br.ufcg.spg.constraint.rule;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Rules (2) and (3) state the type constraints induced by field access
 * operations. For a field access E0.f that refers to a field F, Rule (2) states
 * that the type of the entire expression E0.f is defined to be the same as the
 * declared type of field F.
 */
public class Rule2 extends RuleBase {
  @Override
  public boolean isValidRule(final ASTNode node) {
    final FieldAccess fldAcc = (FieldAccess) node;
    final IVariableBinding bd = fldAcc.resolveFieldBinding();
    // Type of the field
    final ITypeBinding fieldType = bd.getType();
    // Type of the entire expression
    final ITypeBinding expBinding = fldAcc.resolveTypeBinding();
    if (fieldType != null && expBinding != null) {
      final String fieldTypeStr = fieldType.getQualifiedName();
      final String exprTypeStr = expBinding.getQualifiedName();
      return fieldTypeStr.equals(exprTypeStr);
    }
    return false;
  }

  @Override
  public boolean isApplicableTo(final ASTNode node) {
    return node.getNodeType() == ASTNode.FIELD_ACCESS;
  }
}
