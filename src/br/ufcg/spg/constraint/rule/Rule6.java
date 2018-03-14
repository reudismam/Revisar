package br.ufcg.spg.constraint.rule;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Checks that in a method call E0.M, a method with the same 
 * signature as M must be declared in [E0] or one of its supertype.
 */
public class Rule6 extends RuleBase {

  @Override
  public boolean isValidRule(final ASTNode node) {
    final MethodInvocation invocation = (MethodInvocation) node;
    final Expression expression = invocation.getExpression();
    if (expression == null) {
      return false;
    }
    final ITypeBinding callerType = expression.resolveTypeBinding();
    if (callerType == null) {
      return false;
    }
    final IMethodBinding [] methodBindings = callerType.getDeclaredMethods();
    final SimpleName simmpleName = invocation.getName();
    final String methodName = simmpleName.getIdentifier();
    for (final IMethodBinding method : methodBindings) {
      final String name = method.getName();
      if (name.equals(methodName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isApplicableTo(final ASTNode node) {
    return node instanceof MethodInvocation;
  }
}
