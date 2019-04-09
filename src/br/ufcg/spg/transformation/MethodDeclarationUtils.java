package br.ufcg.spg.transformation;

import br.ufcg.spg.type.TypeUtils;
import org.eclipse.jdt.core.dom.*;

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

  public static Type addMethodBasedOnMethodInvocation(CompilationUnit unit, Type type, MethodInvocation invocation, CompilationUnit templateClass) throws IOException {
    Type classType = TypeUtils.extractType(invocation.getExpression(), invocation.getAST());
    boolean isStatic = classType.toString().equals("void") && !(invocation.getExpression() instanceof MethodInvocation);
    List<ASTNode> arguments = (List<ASTNode>) invocation.arguments();
    return MethodInvocationUtils.processMethodInvocation(unit, invocation, templateClass, invocation.getName(), type, arguments, isStatic, false);
  }

  public static MethodDeclaration createMethod(CompilationUnit templateClass,
                                               SimpleName methodName, Type returnType,
                                               boolean isStatic, boolean isConstructor,
                                               List<Type> argTypes, List<String> varNames) {
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    MethodDeclaration mDecl = templateClass.getAST().newMethodDeclaration();
    if (!isConstructor) {
      mDecl = setReturnType(returnType, templateClass, mDecl);
    }
    mDecl.setConstructor(isConstructor);
    addBody(templateClass, mDecl);
    addThrowStatement(mDecl);
    setName(mDecl, methodName);
    addModifier(mDecl, Modifier.ModifierKeyword.PUBLIC_KEYWORD);
    if (isStatic) {
      addModifier(mDecl, Modifier.ModifierKeyword.STATIC_KEYWORD);
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
}
