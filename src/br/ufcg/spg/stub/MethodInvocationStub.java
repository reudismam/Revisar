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

  public static void stub(CompilationUnit unit, MethodInvocation invocation, CompilationUnit templateClass,
                          SimpleName methodName, Type returnType,
                          List<ASTNode> arguments, boolean isStatic, boolean isConstructor) throws IOException {
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    if (classDecl.getName().toString().contains("Exception")) {
      return;
    }
    List<String> varNames = ParameterUtils.getVarNames(arguments);
    List<Type> argTypes = ParameterUtils.getArgTypes(unit, invocation, arguments);
    MethodDeclaration duplicate = ParameterUtils.findMethod(templateClass, argTypes, methodName.toString());
    System.out.println(duplicate);
    if (duplicate != null && returnType != null) {
      if (duplicate.getReturnType2().toString().contains("syntethic") && !returnType.toString().contains("syntethic")) {
        classDecl.bodyDeclarations().remove(duplicate);
      }
      else if (!duplicate.getReturnType2().toString().contains("syntethic") && returnType.toString().contains("syntethic")) {
        return;
      }
    }
    createMethod(templateClass, methodName, returnType, isStatic, isConstructor, argTypes, varNames);
  }

  /*public static MethodDeclaration createMethod(CompilationUnit unit, MethodInvocation invocation, CompilationUnit templateClass,
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
    mDecl = ParameterUtils.addParameters(unit, invocation, arguments, templateClass, mDecl);
    return mDecl;
  }*/

  /*public static void stub(CompilationUnit unit, MethodInvocation invocation, CompilationUnit templateClass,
                          SimpleName methodName, Type returnType,
                          List<ASTNode> arguments, boolean isStatic, boolean isConstructor) throws IOException {
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    if (classDecl.getName().toString().contains("Exception")) {
      return;
    }
    createMethod(unit, invocation, templateClass, methodName, returnType, arguments, isStatic, isConstructor);
  }*/

  private static MethodDeclaration createMethod(CompilationUnit templateClass,
                                               SimpleName methodName, Type returnType,
                                                boolean isStatic, boolean isConstructor,
                                                List<Type> argTypes, List<String> varNames) {
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
    mDecl = ParameterUtils.addParameter(argTypes, varNames, templateClass, mDecl);
    classDecl.bodyDeclarations().add(mDecl);
    return mDecl;
  }

  public static MethodDeclaration createMethod(CompilationUnit unit, MethodInvocation invocation, CompilationUnit templateClass,
                                  SimpleName methodName, Type returnType,
                                  List<ASTNode> arguments, boolean isStatic, boolean isConstructor) throws IOException {
    List<Type> argTypes = ParameterUtils.getArgTypes(unit, invocation, arguments);
    List<String> varNames = ParameterUtils.getVarNames(arguments);
    return createMethod(templateClass, methodName, returnType, isStatic, isConstructor, argTypes, varNames);
  }

  public static void processMethodInvocationChain(CompilationUnit unit, MethodInvocation methodInvocation, CompilationUnit templateChain) throws IOException {
    TypeDeclaration typeDeclaration = ClassUtils.getTypeDeclaration(templateChain);
    Type returnType = SyntheticClassUtils.getSyntheticType(unit.getAST(), typeDeclaration.getName());
    if (methodInvocation.getExpression() instanceof MethodInvocation) {
      MethodInvocation chain = (MethodInvocation) methodInvocation.getExpression();
      while (chain.getExpression() instanceof  MethodInvocation) {
        stub(unit, chain, templateChain, chain.getName(), returnType, chain.arguments(), false, false);
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
