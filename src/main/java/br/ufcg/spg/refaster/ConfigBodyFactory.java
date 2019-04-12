package br.ufcg.spg.refaster;

import br.ufcg.spg.expression.ExpressionManager;
import br.ufcg.spg.refaster.config.ReturnStatementConfig;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class ConfigBodyFactory {
  
  /**
   * Factory class.
   */
  public static IConfigBody getConfigBody(
      final ReturnStatementConfig rconf, final ASTNode template,
      final ReturnStatement reStatement, final AST ast) {
    final MethodDeclaration method = rconf.getMethod();
    final Expression srcExpression = ExpressionManager.expression(template);
    IConfigBody config;
    if (srcExpression != null) {
      config = new ExpressionBodyConfig(srcExpression, method, reStatement, ast);
      return config;
    }
    if (template instanceof Statement) {
      config = new StatementBodyConfig(template, method, ast);
      return config;
    }
    if (template instanceof SingleVariableDeclaration) {
      config = new SingleVariableDeclarationBodyConfig(template, method, ast);
      return config;
    }
    if (template instanceof VariableDeclarationFragment) {
      config = new VariableDeclarationFragmentBodyConfig(template, method, ast);
      return config;
    }
    ASTNode node = rconf.getTarget();
    List<ASTNode> nodes = rconf.getNodes();
    if (node instanceof SimpleType) {
      config = new TypeBodyConfig(node, nodes, template, method, reStatement, ast);
      return config;
    }
    if (node instanceof ParameterizedType) {
      config = new TypeBodyConfig(node, nodes, template, method, reStatement, ast);
      return config;
    }
    if (node instanceof ArrayType) {
      config = new TypeBodyConfig(node, nodes, template, method, reStatement, ast);
      return config;
    }
    if (node instanceof TypeParameter) {
      config = new TypeBodyConfig(node, nodes, template, method, reStatement, ast);
      return config;
    }
    config = new NoneBodyConfig(method);
    return config;
  }
}
