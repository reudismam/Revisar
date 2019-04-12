package br.ufcg.spg.constraint.rule;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * We say that a call E0.m(E1, ··· , Ek) is to a method M if, at runtime, the
 * call will be dispatched to M or to some method that overrides M.
 * Statically-dispatched, or direct, calls are handled by similar rules not
 * shown here. Furthermore, the type [Ei] of each actual parameter Ei must be
 * the same as, or a subtype of, the type [Param(M,i)] of the corresponding
 * formal parameter Param(M,i) (Rule (5))
 */
public class Rule5 extends RuleBase {

  @Override
  public boolean isValidRule(final ASTNode node) {
    final MethodInvocation invocation = (MethodInvocation) node;
    final List<?> args = invocation.arguments();
    final List<Expression> arguments = new ArrayList<>();
    for (final Object obj : args) {
      final Expression argument = (Expression) obj;
      arguments.add(argument);
    }
    final IMethodBinding method = invocation.resolveMethodBinding();
    //if method binding could not be resolved.
    if (method == null) {
      return false;
    }
    final ITypeBinding [] paramTypes = method.getParameterTypes();
    boolean sameParam = true;
    if (paramTypes.length != arguments.size()) {
      return false;
    }
    for (int j = 0; j < paramTypes.length; j++) {
      final Expression arg = arguments.get(j);
      final ITypeBinding argType = arg.resolveTypeBinding();
      final ITypeBinding paramType = paramTypes[j];
      //In case, argType is null
      if (argType == null) {
        return false;
      }
      if (!argType.isAssignmentCompatible(paramType)) {
        sameParam = false;
      }
    }
    if (sameParam) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isApplicableTo(final ASTNode node) {
    return node instanceof MethodInvocation;
  }
}
