package br.ufcg.spg.refaster;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;

public class ExpressionBodyConfig implements IConfigBody {
  private Expression expression;
  private final MethodDeclaration method;
  private final ReturnStatement reStatement;
  private final AST ast;

  /**
   * Constructor.
   */
  public ExpressionBodyConfig(final Expression expression, final MethodDeclaration method,
      final ReturnStatement reStatement, final AST ast) {
    super();
    this.expression = expression;
    this.method = method;
    this.reStatement = reStatement;
    this.ast = ast;
  }

  @SuppressWarnings("unchecked")
  @Override
  public MethodDeclaration config() {
    expression = (Expression) ASTNode.copySubtree(ast, expression);
    reStatement.setExpression(expression);
    method.getBody().statements().remove(0);
    method.getBody().statements().add(reStatement);
    return method;
  }

  @Override
  public MethodDeclaration configReturnType(ASTNode node, 
      CompilationUnit rule, MethodDeclaration method) {
    method = ReturnTypeTranslator.config(node, rule, method);
    return method;
  }
}
