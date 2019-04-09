package br.ufcg.spg.transformation;

import br.ufcg.spg.stub.StubUtils;
import br.ufcg.spg.type.TypeUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MethodInvocationUtils {

  private MethodInvocationUtils() {
  }

  public static Type processMethodInvocation(CompilationUnit unit, MethodInvocation invocation, CompilationUnit templateClass,
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
      if (isSynthetic(duplicate.getReturnType2()) && !isSynthetic(returnType)) {
        classDecl.bodyDeclarations().remove(duplicate);
      }
      else if (!isSynthetic(duplicate.getReturnType2()) && isSynthetic(returnType)) {
        return duplicate.getReturnType2();
      }
    }
    MethodDeclarationUtils.createMethod(templateClass, methodName, returnType, isStatic, isConstructor, argTypes, varNames);
    return returnType;
  }

  private static boolean isSynthetic(Type type) {
    return type.toString().contains("syntethic");
  }

  public static List<Type> returnType(CompilationUnit unit, Type classTpe, String methodName) throws IOException {
    List<ASTNode> invocations = StubUtils.getNodes(unit, ASTNode.ASSIGNMENT);
    List<Type> types = new ArrayList<>();
    for (ASTNode node : invocations) {
      Assignment assignment = (Assignment) node;
      if (assignment.getRightHandSide() instanceof  MethodInvocation) {
        MethodInvocation invocation = (MethodInvocation) assignment.getRightHandSide();
        CompilationUnit templateClass = ClassUtils.getTemplateClassFromExpression(unit, invocation.getExpression());
        TypeDeclaration declaration = ClassUtils.getTypeDeclaration(templateClass);
        String typeName = NameUtils.extractSimpleName(classTpe);
        String className = declaration.getName().toString();
        Type leftType = TypeUtils.extractType(assignment.getLeftHandSide(), invocation.getAST());
        if (invocation.getName().toString().equals(methodName) && typeName.equals(className)) {
          String simpleName = NameUtils.extractSimpleName(leftType);
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
          String simpleName = NameUtils.extractSimpleName(leftType);
          leftType = ImportUtils.getTypeBasedOnImports(unit, simpleName);
          types.add(leftType);
        }
      }
    }
    return types;
  }

  public static void processMethodInvocationChain(CompilationUnit unit, MethodInvocation methodInvocation, CompilationUnit templateChain) throws IOException {
    TypeDeclaration typeDeclaration = ClassUtils.getTypeDeclaration(templateChain);
    Type returnType = SyntheticClassUtils.getSyntheticType(unit.getAST(), typeDeclaration.getName());
    if (methodInvocation.getExpression() instanceof MethodInvocation) {
      MethodInvocation chain = (MethodInvocation) methodInvocation.getExpression();
      while (chain.getExpression() instanceof  MethodInvocation) {
        processMethodInvocation(unit, chain, templateChain, chain.getName(), returnType, chain.arguments(), false, false);
        chain = (MethodInvocation) chain.getExpression();
      }
      if (chain.getExpression() == null) {
        return;
      }
      CompilationUnit templateClass = ClassUtils.getTemplateClassFromExpression(unit, chain.getExpression());
      if (templateClass == null) {
        return;
      }
      MethodDeclarationUtils.addMethodBasedOnMethodInvocation(unit, returnType, chain, templateClass);
    }
  }
}
