package br.ufcg.spg.constraint.rule;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * We say that a call E0.m(E1, ··· , Ek) is to a method M if, at runtime, 
 * the call will be dispatched to M or to some method that overrides M. 
 * Statically-dispatched, or direct, calls are handled by similar rules not shown here.
 * Rule (4) defines the type of the call expression to be the same as M’s return type.
 */
public class Rule4 extends RuleBase {
  
  @Override
  public boolean isValidRule(final ASTNode node) {
    final MethodInvocation invocation = (MethodInvocation) node;
    //Type of the call expression
    final ITypeBinding callType = invocation.resolveTypeBinding();
    if (callType == null) {
      return false;
    }
    final IMethodBinding method = invocation.resolveMethodBinding();
    final ITypeBinding returnType = method.getReturnType();
    final String callerTypeStr = callType.getQualifiedName();
    final String returnTypeStr = returnType.getQualifiedName();
    return callerTypeStr.equals(returnTypeStr);
  }

  @Override
  public boolean isApplicableTo(final ASTNode node) {
    return node instanceof MethodInvocation;
  }
}
