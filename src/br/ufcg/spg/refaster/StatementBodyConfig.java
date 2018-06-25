package br.ufcg.spg.refaster;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

public class StatementBodyConfig implements IConfigBody {
  private final ASTNode template;
  private final MethodDeclaration method;
  private final AST ast;

  /**
   * Constructor.
   */
  public StatementBodyConfig(final ASTNode template, final MethodDeclaration method,
      final AST ast) {
    super();
    this.template = template;
    this.method = method;
    this.ast = ast;
  }

  @SuppressWarnings("unchecked")
  @Override
  public MethodDeclaration config() {
    Statement statement = (Statement) template;
    statement = (Statement) ASTNode.copySubtree(ast, statement);
    method.getBody().statements().remove(0);
    method.getBody().statements().add(statement);
    return method;
  }
}
