package br.ufcg.spg.refaster;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import br.ufcg.spg.type.TypeUtils;

public class VariableDeclarationFragmentBodyConfig implements IConfigBody {
  private final ASTNode template;
  private final MethodDeclaration method;
  private final AST ast;

  /**
   * Constructor.
   */
  public VariableDeclarationFragmentBodyConfig(final ASTNode template, final MethodDeclaration method,
      final AST ast) {
    super();
    this.template = template;
    this.method = method;
    this.ast = ast;
  }

  @Override
  public MethodDeclaration config() {
    final VariableDeclarationFragment varFrag = (VariableDeclarationFragment) template;
    SimpleName name = varFrag.getName();
    name = (SimpleName) ASTNode.copySubtree(ast, name);
    Expression initializer = varFrag.getInitializer();
    Statement statement = null;
    if (initializer != null) {
      initializer = (Expression) ASTNode.copySubtree(ast, initializer);
      final Assignment assignment = ast.newAssignment();
      assignment.setLeftHandSide(name);
      assignment.setRightHandSide(initializer);
      statement = ast.newExpressionStatement(assignment);
    } else {
      Type stmType = TypeUtils.extractType(varFrag, ast);
      stmType = (Type) ASTNode.copySubtree(ast, stmType);
      statement = ast.newVariableDeclarationStatement(varFrag);
      ((VariableDeclarationStatement) statement).setType(stmType);
    }
    statement = (Statement) ASTNode.copySubtree(ast, statement);
    method.getBody().statements().remove(0);
    method.getBody().statements().add(statement);
    return method;
  }
}
