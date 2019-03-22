package br.ufcg.spg.transformation;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class JDTElementUtils {
  public static void setName(TypeDeclaration mDecl, SimpleName name) {
    AST ast = mDecl.getAST();
    name = (SimpleName) ASTNode.copySubtree(ast, name);
    mDecl.setName(name);
  }
}
