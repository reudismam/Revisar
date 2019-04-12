package br.ufcg.spg.constraint.rule;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * We say that a call E0.m(E1, ··· , Ek) is to a method M if, at runtime, 
 * the call will be dispatched to M or to some method that overrides M. 
 * Statically-dispatched, or direct, calls are handled by similar rules not shown here.
 * Rule (4) defines the type of the call expression to be the same as M’s return type.
 * Rules (7)-(8) are similar to rules (4)-(5).
 */
public class Rule7 extends RuleBase {

  @Override
  public boolean isValidRule(final ASTNode node) {
    if (!(node instanceof ClassInstanceCreation)) {
      return false;   
    }
    final ClassInstanceCreation classInstCrt = (ClassInstanceCreation) node;
    //Type of the call expression
    final ITypeBinding callType = classInstCrt.resolveTypeBinding();
    if (callType == null) {
      return false;
    }
    final IMethodBinding constructor = classInstCrt.resolveConstructorBinding();
    if (constructor == null) {
      return false;
    }
    final ITypeBinding returnType = constructor.getReturnType();
    final String callerTypeStr = callType.getQualifiedName();
    final String returnTypeStr = returnType.getQualifiedName();
    return callerTypeStr.equals(returnTypeStr);
  }

  @Override
  public boolean isApplicableTo(final ASTNode node) {
    return node instanceof ClassInstanceCreation;
  }
}
