package br.ufcg.spg.stub;

import br.ufcg.spg.refaster.ParameterUtils;
import br.ufcg.spg.refaster.ClassUtils;
import br.ufcg.spg.transformation.JDTElementUtils;
import br.ufcg.spg.transformation.MethodDeclarationUtils;
import br.ufcg.spg.transformation.SyntheticClassUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.util.List;

public class MethodInvocationStub {
  public static void stub(CompilationUnit unit, CompilationUnit templateClass,
                          SimpleName methodName, Type returnType,
                          List<ASTNode> arguments, boolean isStatic, boolean isConstructor) throws IOException {
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    if (classDecl.getName().toString().contains("Exception")) {
      return;
    }
   createMethod(unit, templateClass, methodName, returnType, arguments, isStatic, isConstructor);
  }

  public static void createMethod(CompilationUnit unit, CompilationUnit templateClass,
                          SimpleName methodName, Type returnType,
                          List<ASTNode> arguments, boolean isStatic, boolean isConstructor) throws IOException {
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    MethodDeclaration mDecl = templateClass.getAST().newMethodDeclaration();
    if (!isConstructor) {
      mDecl = MethodDeclarationUtils.setReturnType(returnType, templateClass, mDecl);
    }
    mDecl.setConstructor(isConstructor);
    MethodDeclarationUtils.addBody(templateClass, mDecl);
    MethodDeclarationUtils.addThrowStatement(mDecl);
    MethodDeclarationUtils.setName(mDecl, methodName);
    MethodDeclarationUtils.addModifier(mDecl, Modifier.ModifierKeyword.PUBLIC_KEYWORD);
    if (isStatic) {
      MethodDeclarationUtils.addModifier(mDecl, Modifier.ModifierKeyword.STATIC_KEYWORD);
    }
    mDecl = ParameterUtils.addParameters(unit, arguments, templateClass, mDecl);
    classDecl.bodyDeclarations().add(mDecl);
    //JDTElementUtils.saveClass(unit, templateClass);
  }

  public static void processMethodInvocationChain(CompilationUnit unit, MethodInvocation methodInvocation, CompilationUnit templateChain) throws IOException {
    TypeDeclaration typeDeclaration = ClassUtils.getTypeDeclaration(templateChain);
    Type returnType = SyntheticClassUtils.getSyntheticType(unit.getAST(), typeDeclaration.getName());
    if (methodInvocation.getExpression() instanceof MethodInvocation) {
      MethodInvocation chain = (MethodInvocation) methodInvocation.getExpression();
      while (chain.getExpression() instanceof  MethodInvocation) {
        stub(unit, templateChain, chain.getName(), returnType, chain.arguments(), false, false);
        chain = (MethodInvocation) chain.getExpression();
      }
      if (chain.getExpression() == null) {
        return;
      }
      CompilationUnit templateClass = ClassUtils.getTemplateClassBasedOnInvocation(unit, chain.getExpression());
      if (templateClass == null) {
        return;
      }
      MethodDeclarationUtils.addMethodBasedOnMethodInvocation(unit, returnType, chain, templateClass);
      JDTElementUtils.saveClass(unit, templateClass);
      JDTElementUtils.saveClass(unit, templateChain);
    }
  }
}
