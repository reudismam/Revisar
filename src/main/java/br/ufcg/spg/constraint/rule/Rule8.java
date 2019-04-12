package br.ufcg.spg.constraint.rule;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * We say that a call E0.m(E1, ··· , Ek) is to a method M if, at runtime, the
 * call will be dispatched to M or to some method that overrides M.
 * Statically-dispatched, or direct, calls are handled by similar rules not
 * shown here. Furthermore, the type [Ei] of each actual parameter Ei must be
 * the same as, or a subtype of, the type [Param(M,i)] of the corresponding
 * formal parameter Param(M,i) (Rule (5)). Rules (7)-(8) are similar to rules (4)-(5).
 */
public class Rule8 extends RuleBase {

  @Override
  public boolean isValidRule(final ASTNode node) {
    final ClassInstanceCreation classInstCrt = (ClassInstanceCreation) node;
    final List<?> args = classInstCrt.arguments();
    final List<Expression> arguments = new ArrayList<>();
    for (final Object obj : args) {
      final Expression argument = (Expression) obj;
      arguments.add(argument);
    }
    final IMethodBinding constructor = classInstCrt.resolveConstructorBinding();
    if (constructor == null) {
      return false;
    }
    final ITypeBinding [] paramTypes = constructor.getParameterTypes();
    boolean sameParam = true;
    for (int j = 0; j < paramTypes.length; j++) {
      final Expression arg = arguments.get(j);
      final ITypeBinding argType = arg.resolveTypeBinding();
      final ITypeBinding paramType = paramTypes[j];
      //In case we cannot determine the type of the arguments.
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
    return node instanceof ClassInstanceCreation;
  }
}
