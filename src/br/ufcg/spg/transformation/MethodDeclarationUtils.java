package br.ufcg.spg.transformation;

import br.ufcg.spg.stub.MethodInvocationStub;
import br.ufcg.spg.type.TypeUtils;
import org.eclipse.jdt.core.dom.*;
import sun.java2d.pipe.SpanShapeRenderer;

import java.io.IOException;
import java.util.List;

public class MethodDeclarationUtils {
  /**
   * Sets the return type for Refaster Rules.
   *
   * @param type
   *          type to be returned
   * @param cUnit
   *          Refaster template rule
   * @param method
   *          method that receives the return type
   */
  public static MethodDeclaration setReturnType(Type type, final CompilationUnit cUnit,
                                                MethodDeclaration method) {
    final AST ast = cUnit.getAST();
    type = (Type) ASTNode.copySubtree(ast, type);
    method = (MethodDeclaration) ASTNode.copySubtree(ast, method);
    method.setReturnType2(type);
    return method;
  }

  public static void addBody(CompilationUnit cUnit, MethodDeclaration mDecl) {
    Block body = cUnit.getAST().newBlock();
    mDecl.setBody(body);
  }

  public static void setName(MethodDeclaration mDecl, SimpleName name) {
    AST ast = mDecl.getAST();
    name = (SimpleName) ASTNode.copySubtree(ast, name);
    mDecl.setName(name);
  }

  public static void addModifier(MethodDeclaration mDecl, Modifier.ModifierKeyword modifier) {
    AST ast = mDecl.getAST();
    mDecl.modifiers().add(ast.newModifier(modifier));
  }

  public static ThrowStatement addThrowStatement(MethodDeclaration mDecl) {
    ThrowStatement statement = mDecl.getAST().newThrowStatement();
    SimpleName name = statement.getAST().newSimpleName("UnsupportedOperationException");
    ClassInstanceCreation instance  = mDecl.getAST().newClassInstanceCreation();
    Name n = instance.getAST().newName("java.lang");
    Type nType = instance.getAST().newNameQualifiedType(n, name);
    instance.setType(nType);
    statement.setExpression(instance);
    mDecl.getBody().statements().add(statement);
    return statement;
  }

  public static void addMethodBasedOnMethodInvocation(CompilationUnit unit, Type type, MethodInvocation invocation, CompilationUnit templateClass) throws IOException {
    Type classType = TypeUtils.extractType(invocation.getExpression(), invocation.getAST());
    boolean isStatic = classType.toString().equals("void");
    List<ASTNode> arguments = (List<ASTNode>) invocation.arguments();
    MethodInvocationStub.stub(unit, invocation, templateClass, invocation.getName(), type, arguments, isStatic, false);
  }
}
