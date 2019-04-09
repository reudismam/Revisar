package br.ufcg.spg.stub;

import br.ufcg.spg.refaster.ParameterUtils;
import br.ufcg.spg.refaster.ClassUtils;
import br.ufcg.spg.transformation.ImportUtils;
import br.ufcg.spg.transformation.JDTElementUtils;
import br.ufcg.spg.transformation.MethodDeclarationUtils;
import br.ufcg.spg.transformation.SyntheticClassUtils;
import br.ufcg.spg.type.TypeUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MethodInvocationStub {

  public static Type stub(CompilationUnit unit, MethodInvocation invocation, CompilationUnit templateClass,
                          SimpleName methodName, Type returnType,
                          List<ASTNode> arguments, boolean isStatic, boolean isConstructor) throws IOException {
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    if (classDecl.getName().toString().contains("Exception")) {
      return null;
    }
    List<String> varNames = ParameterUtils.getVarNames(arguments);
    List<Type> argTypes = ParameterUtils.getArgTypes(unit, invocation, arguments);
    MethodDeclaration duplicate = ParameterUtils.findMethod(templateClass, argTypes, methodName.toString());
    if (duplicate != null && returnType != null) {
      if (duplicate.getReturnType2().toString().contains("syntethic") && !returnType.toString().contains("syntethic")) {
        classDecl.bodyDeclarations().remove(duplicate);
      }
      else if (!duplicate.getReturnType2().toString().contains("syntethic") && returnType.toString().contains("syntethic")) {
        return duplicate.getReturnType2();
      }
    }
    createMethod(templateClass, methodName, returnType, isStatic, isConstructor, argTypes, varNames);
    return returnType;
  }

  public static List<Type> returnType(CompilationUnit unit, Type classTpe, String methodName) throws IOException {
    List<ASTNode> invocations = StubUtils.getNodes(unit, ASTNode.ASSIGNMENT);
    List<Type> types = new ArrayList<>();
    for (ASTNode node : invocations) {
      Assignment assignment = (Assignment) node;
      if (assignment.getRightHandSide() instanceof  MethodInvocation) {
        MethodInvocation invocation = (MethodInvocation) assignment.getRightHandSide();
        CompilationUnit templateClass = ClassUtils.getTemplateClassBasedOnInvocation(unit, invocation.getExpression());
        TypeDeclaration declaration = ClassUtils.getTypeDeclaration(templateClass);
        String typeName = JDTElementUtils.extractSimpleName(classTpe);
        String className = declaration.getName().toString();
        Type leftType = TypeUtils.extractType(assignment.getLeftHandSide(), invocation.getAST());
        if (invocation.getName().toString().equals(methodName) && typeName.equals(className)) {
          String simpleName = JDTElementUtils.extractSimpleName(leftType);
          leftType = ImportUtils.getTypeBasedOnImports(unit, simpleName);
          types.add(leftType);
        }
      }
    }
    List<ASTNode> variableDeclaration = StubUtils.getNodes(unit, ASTNode.VARIABLE_DECLARATION_STATEMENT);
    for (ASTNode node : variableDeclaration) {
      VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
      Type leftType = TypeUtils.extractType(statement, statement.getAST());
      if (statement.fragments().get(0) instanceof  MethodInvocation) {
        MethodInvocation invocation = (MethodInvocation) statement.fragments().get(0);
        if (invocation.getName().toString().equals(methodName)) {
          System.out.println(leftType);
        }
      }
    }
    return types;
  }

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
