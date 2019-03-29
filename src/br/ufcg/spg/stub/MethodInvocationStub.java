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
                          SimpleName methodName, Type returnType, List<ASTNode> arguments, boolean isStatic) throws IOException {
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    System.out.println("Before\n");
    System.out.println(templateClass.toString());
    MethodDeclaration declaration = templateClass.getAST().newMethodDeclaration();
    MethodDeclaration mDecl = MethodDeclarationUtils.setReturnType(returnType, templateClass, declaration);
    MethodDeclarationUtils.addBody(templateClass, mDecl);
    MethodDeclarationUtils.addThrowStatement(mDecl);
    MethodDeclarationUtils.setName(mDecl, methodName);
    MethodDeclarationUtils.addModifier(mDecl, Modifier.ModifierKeyword.PUBLIC_KEYWORD);
    if (isStatic) {
      MethodDeclarationUtils.addModifier(mDecl, Modifier.ModifierKeyword.STATIC_KEYWORD);
    }
    mDecl = ParameterUtils.addParameters(unit, arguments, templateClass, mDecl);
    System.out.println("After\n");
    classDecl.bodyDeclarations().add(mDecl);
    System.out.println(templateClass.toString());
    JDTElementUtils.saveClass(templateClass, classDecl);
  }

  public static void processMethodInvocationChain(CompilationUnit unit, MethodInvocation methodInvocation, CompilationUnit templateChain) throws IOException {
    TypeDeclaration typeDeclaration = ClassUtils.getTypeDeclaration(templateChain);
    Type returnType = SyntheticClassUtils.getSyntheticType(unit, typeDeclaration.getName());
    stub(unit, templateChain, methodInvocation.getName(), returnType, methodInvocation.arguments(), false);
    if (methodInvocation.getExpression() instanceof MethodInvocation) {
      MethodInvocation chain = (MethodInvocation) methodInvocation.getExpression();
      while (chain.getExpression() instanceof  MethodInvocation) {
        stub(unit, templateChain, chain.getName(), returnType, chain.arguments(), false);
        chain = (MethodInvocation) chain.getExpression();
      }
      System.out.println(templateChain);
      CompilationUnit templateClass = ClassUtils.getTemplateClassBasedOnInvocation(unit, chain.getExpression());
      MethodDeclarationUtils.addMethodBasedOnMethodInvocation(unit, returnType, chain, templateClass);
      System.out.println("The final template class is: \n" + templateClass);
      JDTElementUtils.saveClass(templateClass, ClassUtils.getTypeDeclaration(templateChain));
    }
  }
}
