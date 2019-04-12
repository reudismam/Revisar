package br.ufcg.spg.constraint.rule;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class Rule1 extends RuleBase {
  @Override
  public boolean isValidRule(final ASTNode node) {
    return isValid1(node) || isValid2(node);
  }
  
  private boolean isValid1(final ASTNode node) {
    if (!(node instanceof Assignment)) {
      return false;
    }
    final Assignment assignment = (Assignment) node;
    final Expression left = assignment.getLeftHandSide();
    final Expression right = assignment.getRightHandSide();
    final ITypeBinding leftBinding = left.resolveTypeBinding();
    final ITypeBinding rightBinding = right.resolveTypeBinding();
    if (rightBinding == null || leftBinding == null) {
      return false;
    }
    final boolean isAssigned = rightBinding.isAssignmentCompatible(leftBinding);
    return isAssigned;
  }
  
  private boolean isValid2(final ASTNode node) {
    if (!(node instanceof VariableDeclarationFragment)) {
      return false;
    }
    final VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
    final SimpleName left = fragment.getName();
    final Expression right = fragment.getInitializer();
    if (left == null || right == null) {
      return false;
    }
    final ITypeBinding leftBinding = left.resolveTypeBinding();
    final ITypeBinding rightBinding = right.resolveTypeBinding();
    if (leftBinding == null || rightBinding == null) {
      return false;
    }
    final boolean isAssigned = rightBinding.isAssignmentCompatible(leftBinding);
    return isAssigned;
  }

  @Override
  public boolean isApplicableTo(final ASTNode node) {
    if (node instanceof Assignment) {
      return true;
    }
    return node instanceof VariableDeclarationFragment;
  }
}
