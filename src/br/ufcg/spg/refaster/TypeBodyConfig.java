package br.ufcg.spg.refaster;

import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.type.TypeUtils;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class TypeBodyConfig implements IConfigBody {
  private final ASTNode template;
  private final MethodDeclaration method;
  private final List<ASTNode> nodes;
  private final AST ast;
  private final ReturnStatement reStatement;
  private final ASTNode node;

  /**
   * Constructor.
   */
  public TypeBodyConfig(final ASTNode node, final List<ASTNode> nodes, final ASTNode template, 
      final MethodDeclaration method, final ReturnStatement reStatement,
      final AST ast) {
    super();
    this.node = node;
    this.nodes = nodes;
    this.template = template;
    this.method = method;
    this.reStatement = reStatement;
    this.ast = ast;
  }

  @Override
  public MethodDeclaration config() {
    try {
      final ASTNode nodeForType = TypeUtils.nodeForType(node);
      Type typeToAnalyze = TypeUtils.extractType(nodeForType, ast);
      final MethodDeclaration targetMethod = targetMethod();
      final ReturnStatement returnStm = (ReturnStatement) targetMethod.getBody().statements().get(0);
      final MethodInvocation invocation = (MethodInvocation) returnStm.getExpression();
      invocation.typeArguments().remove(0);
      Expression expression = null;
      if (!nodes.isEmpty()) {
        if (template instanceof ParameterizedType) {
          final ParameterizedType paramType = (ParameterizedType) template;
          Type type = TypeUtils.typeFromParameterizedType(ast, paramType);
          type = (Type) ASTNode.copySubtree(invocation.getAST(), type);
          invocation.typeArguments().add(type);
          expression = invocation;
        }
      } else {
        typeToAnalyze = (Type) ASTNode.copySubtree(invocation.getAST(), typeToAnalyze);
        invocation.typeArguments().add(typeToAnalyze);
        expression = invocation;
      }
      expression = (Expression) ASTNode.copySubtree(ast, expression);
      reStatement.setExpression(expression);
      method.getBody().statements().remove(0);
      method.getBody().statements().add(reStatement);
      return method;
    } catch (final IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Gets target method for Refaster magic incantation.
   * 
   * @return target method for Refaster magic incantation
   */
  private static MethodDeclaration targetMethod() throws IOException {
    final String refasterTemplates = "src/br/ufcg/spg/refaster/RefasterTemplates.java";
    final CompilationUnit comUnit = JParser.parse(refasterTemplates);
    final TypeDeclaration type = (TypeDeclaration) comUnit.types().get(0);
    final MethodDeclaration[] ms = type.getMethods();
    MethodDeclaration targetMethod = ms[0];
    // Extracting the method for Refaster rule
    // that contains the clazz magic method.
    for (final MethodDeclaration currentMethod : ms) {
      final String name = currentMethod.getName().getIdentifier();
      if (name.equals("clazz")) {
        targetMethod = currentMethod;
      }
    }
    return targetMethod;
  }

}
