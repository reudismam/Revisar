package br.ufcg.spg.constraint.rule;

import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.KindNodeMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.NodeMatchCalculator;

import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;

public class Rule13 extends RuleBase {

  @Override
  public boolean isValidRule(final ASTNode node) {
    final MethodDeclaration methodDcl = (MethodDeclaration) node;
    final IMethodBinding methodType = methodDcl.resolveBinding();
    if (methodType == null) {
      return false;
    }
    final ITypeBinding typeBinding = methodType.getReturnType();
    final IMatcher<ASTNode> returnMatcher = new KindNodeMatcher(ASTNode.RETURN_STATEMENT);
    final MatchCalculator<ASTNode> mcal = new NodeMatchCalculator(returnMatcher);
    final List<ASTNode> returnStms = mcal.getNodes(methodDcl);
    for (final ASTNode ast : returnStms) {
      final ReturnStatement rstm = (ReturnStatement) ast;
      final Expression expr = rstm.getExpression();
      if (expr == null) {
        return false;
      }
      final ITypeBinding exprBinding = expr.resolveTypeBinding();
      //In case, exprBinding is null
      if (exprBinding == null) {
        return false;
      }
      if (!exprBinding.isSubTypeCompatible(typeBinding)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isApplicableTo(final ASTNode node) {
    return node instanceof MethodDeclaration;
  }    
}
