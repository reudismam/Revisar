package br.ufcg.spg.transformation;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

public class FieldDeclarationUtils {
  public static void addModifier(FieldDeclaration fDecl, Modifier.ModifierKeyword modifier) {
    AST ast = fDecl.getAST();
    fDecl.modifiers().add(ast.newModifier(modifier));
  }
}
