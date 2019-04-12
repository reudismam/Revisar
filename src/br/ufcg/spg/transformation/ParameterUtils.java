package br.ufcg.spg.transformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.stub.StubUtils;
import br.ufcg.spg.type.TypeUtils;
import org.eclipse.jdt.core.dom.*;

/**
 * Configures parameters.
 */
public final class ParameterUtils {
  
  private ParameterUtils() {
  }

  /**
   * Adds parameter to method.
   * @param types types to be analyzed
   * @param method method 
   * @return method with parameters added.
   */
  @SuppressWarnings("unchecked")
  public static MethodDeclaration addParameter(final List<Type> types, List<String> varNames, MethodDeclaration method) {
    final AST ast = method.getAST();
    method.parameters().clear();
    for (int i = 0; i < types.size(); i++) {
      Type type = types.get(i);
      String name = varNames.get(i);
      SingleVariableDeclaration singleVariableDeclaration = getSingleVariableDeclaration(type, name, ast);
      singleVariableDeclaration = (SingleVariableDeclaration) ASTNode.copySubtree(ast, singleVariableDeclaration);
      method.parameters().add(singleVariableDeclaration);
    }
    return method;
  }

  private static SingleVariableDeclaration getSingleVariableDeclaration(Type type, String varName, AST ast) {
    final SingleVariableDeclaration singleVariableDeclaration =
        ast.newSingleVariableDeclaration();
    final SimpleName name = ast.newSimpleName(varName);
    singleVariableDeclaration.setName(name);
    type = (Type) ASTNode.copySubtree(ast, type);
    singleVariableDeclaration.setType(type);
    singleVariableDeclaration.setVarargs(false);
    return singleVariableDeclaration;
  }

  public static MethodDeclaration findMethod(CompilationUnit templateClass, List<Type> argTypes, String name) {
    TypeDeclaration typeDeclaration = ClassUtils.getTypeDeclaration(templateClass);
    MethodDeclaration duplicate = null;
    List<ASTNode> nodes = (List<ASTNode>) typeDeclaration.bodyDeclarations();
    for (ASTNode node : nodes) {
      if (node instanceof MethodDeclaration) {
        MethodDeclaration methodDeclaration = (MethodDeclaration) node;
        if (methodDeclaration.getName().toString().equals(name)) {
          List<Type> parameters = new ArrayList<>();
          List<ASTNode> parametersList = (List<ASTNode>) methodDeclaration.parameters();
          for (ASTNode param : parametersList) {
            SingleVariableDeclaration parameter = (SingleVariableDeclaration) param;
            parameters.add(parameter.getType());
          }
          if (parameters.toString().equals(argTypes.toString())) {
            duplicate = methodDeclaration;
          }
        }
      }
    }
    return duplicate;
  }

  public static List<Type> getArgTypes(CompilationUnit unit, MethodInvocation invocation, List<ASTNode> arguments) throws IOException {
    List<Type> argTypes = new ArrayList<>();
    for(ASTNode arg : arguments) {
      Type argType;
      if (arg.toString().equals("null")) {
        argType = TypeUtils.createType(unit.getAST(), "java.lang", "Object");
      } else {
        argType = TypeUtils.extractType(arg, arg.getAST());
      }
      argType = processArgument(unit, invocation, arg, argType);
      //Needed to resolve a bug in eclipse JDT.
      if (argType.toString().contains(".") && !argType.toString().contains("syntethic")) {
        String typeName = NameUtils.extractSimpleName(argType);
        argType = ImportUtils.getTypeBasedOnImports(unit, typeName);
      }
      argTypes.add(argType);
    }
    return argTypes;
  }

  private static Type processArgument(CompilationUnit unit, MethodInvocation invocation, ASTNode arg, Type argType) throws IOException {
    if (arg instanceof MethodInvocation) {
      argType = processMethodInvocationParameter(unit, invocation, arg, argType);
    }
    else if (arg instanceof ClassInstanceCreation) {
      argType = processClassInstanceParameter(unit, arg);
    }
    else if (arg instanceof QualifiedName) {
      argType = processQualifiedNameParameter(unit, (QualifiedName) arg);
    }
    else if (arg instanceof CastExpression) {
      throw new RuntimeException();
    }
    return argType;
  }

  private static Type processQualifiedNameParameter(CompilationUnit unit, QualifiedName arg) throws IOException {
    Type argType;
    QualifiedName qualifiedName = arg;
    ASTNode imp = ImportUtils.findImport(unit, qualifiedName.getQualifier().toString());
    Tuple<String, String> inner = InnerClassUtils.getInnerClassImport(qualifiedName.getQualifier().toString());
    String fullName;
    if (imp != null) {
       fullName = imp.toString().substring(7, imp.toString().indexOf(inner.getItem1())-1);
    }
    else {
      String qualifiedNameStr = "defaultpkg." + qualifiedName.getQualifier().toString();
      fullName = qualifiedNameStr.substring(0, qualifiedNameStr.indexOf(inner.getItem1().trim())-1);
    }
    StubUtils.processInnerClass(unit, inner, fullName);
    Type type = SyntheticClassUtils.getSyntheticType(unit.getAST());
    FieldDeclaration fieldDeclaration = FieldDeclarationUtils.createFieldDeclaration(unit, qualifiedName.getName(), type);
    FieldDeclarationUtils.addModifier(fieldDeclaration, Modifier.ModifierKeyword.STATIC_KEYWORD);
    Type typeOuter = TypeUtils.createType(unit.getAST(), fullName, inner.getItem1());
    Type typeInner = TypeUtils.createType(unit.getAST(), fullName, inner.getItem2());
    CompilationUnit outer = ClassUtils.getTemplateClass(unit, typeOuter);
    CompilationUnit innerClass = ClassUtils.getTemplateClass(unit, typeInner);
    TypeDeclaration outerTypeDeclaration = ClassUtils.getTypeDeclaration(outer);
    TypeDeclaration declaration = InnerClassUtils.getTypeDeclarationIfNeeded(inner.getItem2(), outerTypeDeclaration, innerClass);
    fieldDeclaration = (FieldDeclaration) ASTNode.copySubtree(declaration.getAST(), fieldDeclaration);
    declaration.bodyDeclarations().add(fieldDeclaration);
    ClassUtils.addModifier(declaration, Modifier.ModifierKeyword.STATIC_KEYWORD);
    argType = type;
    return argType;
  }

  private static Type processClassInstanceParameter(CompilationUnit unit, ASTNode arg) throws IOException {
    ClassInstanceCreation initializer = (ClassInstanceCreation) arg;
    Type argType = TypeUtils.extractTypeGlobalAnalysis(unit, arg);
    ExpressionUtils.processExpressionBase(unit, argType, initializer);
    return argType;
  }

  private static Type processMethodInvocationParameter(CompilationUnit unit, MethodInvocation invocation, ASTNode arg, Type argType) {
    MethodInvocation methodInvocationArg = (MethodInvocation) arg;
    try {
      if (((MethodInvocation) arg).getExpression() instanceof QualifiedName) {
        Type classType = TypeUtils.getTypeFromQualifiedName(unit, methodInvocationArg.getExpression());
        ClassUtils.getTemplateClass(unit, classType);
      }
      if (argType.toString().equals("void")) {
        argType = getArgType(unit, invocation);
      }
      argType = StubUtils.processMethodInvocation(unit, argType, arg);
      if (!methodInvocationArg.getExpression().toString().contains(".")) {
        Type newType = ImportUtils.getTypeBasedOnImports(unit, methodInvocationArg.getExpression().toString());
        List<Type> types = MethodInvocationUtils.returnType(unit, newType, methodInvocationArg.getName().toString());
        if (argType != null && argType.toString().contains("syntethic") && !types.isEmpty()) {
          argType = types.get(0);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return argType;
  }

  public static List<String> getVarNames(List<ASTNode> arguments) {
    List<String> varNames = new ArrayList<>();
    for(int i = 0; i < arguments.size(); i++) {
      varNames.add("v_" + i);
    }
    return varNames;
  }

  private static Type getArgType(CompilationUnit unit, MethodInvocation invocation) {
    Type argType;
    Type newParamType = null;
    if (invocation != null) {
      Type classType = TypeUtils.extractType((invocation).getExpression(), invocation.getAST());
      if (classType.isParameterizedType()) {
        ParameterizedType parameterizedType = (ParameterizedType) classType;
        Type paramType = (Type) parameterizedType.typeArguments().get(0);
        String simpleName = NameUtils.extractSimpleName(paramType);
        newParamType = ImportUtils.getTypeBasedOnImports(unit, simpleName);
      }
    }
    if (newParamType != null) {
      argType = newParamType;
    }
    else {
      argType = SyntheticClassUtils.getSyntheticType(unit.getAST());
    }
    return argType;
  }
}
