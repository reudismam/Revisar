package br.ufcg.spg.constraint.rule;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Rules (2) and (3) state the type constraints induced by field access
 * operations. For a field access E0.f that refers to a field F, Rule (3) states
 * that the type of expression E0 must be a subtype of the type in which field F
 * is declared.
 */
public class Rule3 extends RuleBase {
  @Override
  public boolean isValidRule(final ASTNode node) {
    final FieldAccess fldAcc = (FieldAccess) node;
    final IVariableBinding bd = fldAcc.resolveFieldBinding();
    // Type in which field F is declared
    final ITypeBinding fieldDeclClass = bd.getDeclaringClass();
    // Type of the entire expression
    final ITypeBinding expBinding = fldAcc.resolveTypeBinding();
    if (expBinding == null) {
      return false;
    }
    // Get E0
    final Expression exp = fldAcc.getExpression();
    if (exp == null) {
      return false;
    }
    final ITypeBinding E0Type = exp.resolveTypeBinding();
    if (fieldDeclClass != null && E0Type != null) {
      return E0Type.isAssignmentCompatible(fieldDeclClass);
    }
    return false;
  }

  @Override
  public boolean isApplicableTo(final ASTNode node) {
    return node.getNodeType() == ASTNode.FIELD_ACCESS;
  }
}
