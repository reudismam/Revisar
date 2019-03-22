package br.ufcg.spg.transformation;

import org.eclipse.jdt.core.dom.*;
import sun.java2d.pipe.SpanShapeRenderer;

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
}
