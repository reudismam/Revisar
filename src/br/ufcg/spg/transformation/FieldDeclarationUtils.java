package br.ufcg.spg.transformation;

import br.ufcg.spg.refaster.ClassUtils;
import br.ufcg.spg.stub.MethodInvocationStub;
import br.ufcg.spg.type.TypeUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;

public class FieldDeclarationUtils {
  public static void addModifier(FieldDeclaration fDecl, Modifier.ModifierKeyword modifier) {
    AST ast = fDecl.getAST();
    fDecl.modifiers().add(ast.newModifier(modifier));
  }

  public static void processFieldDeclaration(CompilationUnit unit, VariableDeclarationStatement inv, Expression initializer) throws IOException {
    FieldAccess facces = (FieldAccess) initializer;
    Expression expression = facces.getExpression();
    if (expression instanceof MethodInvocation) {
      MethodInvocation methodInvocation = (MethodInvocation) expression;
      if (TypeUtils.extractType(expression, unit.getAST()).toString().equals("void")) {
        CompilationUnit templateChain = SyntheticClassUtils.createSyntheticClass(unit);
        VariableDeclarationFragment vfrag = unit.getAST().newVariableDeclarationFragment();
        SimpleName fieldName = (SimpleName) ASTNode.copySubtree(vfrag.getAST(), facces.getName());
        vfrag.setName(fieldName);
        FieldDeclaration fieldDeclaration = unit.getAST().newFieldDeclaration(vfrag);
        addModifier(fieldDeclaration, Modifier.ModifierKeyword.PUBLIC_KEYWORD);
        Type type = TypeUtils.extractType(inv, inv.getAST());
        type = (Type) ASTNode.copySubtree(type.getAST(), type);
        fieldDeclaration.setType(type);
        ImportDeclaration importDeclaration = (ImportDeclaration) ImportUtils.findImport(unit, type.toString());
        importDeclaration = (ImportDeclaration) ASTNode.copySubtree(templateChain.getAST(), importDeclaration);
        templateChain.imports().add(importDeclaration);
        TypeDeclaration typeDeclaration = ClassUtils.getTypeDeclaration(templateChain);
        fieldDeclaration = (FieldDeclaration) ASTNode.copySubtree(typeDeclaration.getAST(), fieldDeclaration);
        typeDeclaration.bodyDeclarations().add(fieldDeclaration);
        Type returnType = SyntheticClassUtils.getSyntheticType(unit.getAST(), typeDeclaration.getName());
        MethodInvocationStub.stub(unit, templateChain, methodInvocation.getName(), returnType, methodInvocation.arguments(), false, false);
        MethodInvocationStub.processMethodInvocationChain(unit, methodInvocation, templateChain);
        JDTElementUtils.saveClass(templateChain);
      }
    }
  }
}
