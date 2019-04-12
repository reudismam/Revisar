package br.ufcg.spg.stub;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.matcher.InstanceNodeMatcher;
import br.ufcg.spg.matcher.KindNodeMatcher;
import br.ufcg.spg.matcher.calculator.NodeMatchCalculator;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.transformation.ClassUtils;
import br.ufcg.spg.transformation.ParameterUtils;
import br.ufcg.spg.transformation.*;
import br.ufcg.spg.type.TypeUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StubUtils {

  public static void generateStubsForClass(String classFile) throws IOException {
    FileUtils.cleanDirectory(new File("temp/"));
    CompilationUnit unit = JParser.parseFromFile(classFile);
    FileUtils.writeStringToFile(new File("temp/defaultpkg/temp.java"), unit.getRoot().toString());
    stubForVariableDeclaration(unit);
    stubForExpressionStatement(unit);
    stubForThrowStatement(unit);
    stubForIfStatement(unit);
    stubForParameterizedType(unit);
    stubForInfixExpression(unit);
    processImportStatement(unit);
    addImportStatement(unit);
    System.out.println("Finished");
  }

  private static void stubForInfixExpression(CompilationUnit unit) throws IOException {
    List<ASTNode> invocations = getNodes(unit, ASTNode.INFIX_EXPRESSION);
    for(ASTNode ast : invocations) {
      InfixExpression infixExpression = (InfixExpression) ast;
      ExpressionUtils.processExpression(unit, infixExpression, null);
    }
  }

  private static void stubForParameterizedType(CompilationUnit unit) throws IOException {
    List<ASTNode> nodes = getNodes(unit, ASTNode.PARAMETERIZED_TYPE);
    for (ASTNode node : nodes) {
      processTypeParameter(unit, TypeUtils.extractType(node, node.getAST()));
    }
  }

  private static void stubForIfStatement(CompilationUnit unit) throws IOException {
    List<ASTNode> invocations = getNodes(unit, ASTNode.IF_STATEMENT);
    for(ASTNode ast : invocations) {
      IfStatement statement = (IfStatement) ast;
      Type type = unit.getAST().newPrimitiveType(PrimitiveType.BOOLEAN);
      if (statement.getExpression() instanceof PrefixExpression) {
        PrefixExpression infixExpression = (PrefixExpression) statement.getExpression();
        ExpressionUtils.processExpressionBase(unit, type, infixExpression.getOperand());
      }
      ExpressionUtils.processExpression(unit, statement.getExpression(), type);
    }
  }

  private static void stubForThrowStatement(CompilationUnit unit) throws IOException {
    List<ASTNode> invocations = getNodes(unit, ASTNode.THROW_STATEMENT);
    for(ASTNode ast : invocations) {
      ThrowStatement statement = (ThrowStatement) ast;
      Type statementType = TypeUtils.extractType(statement.getExpression(), statement.getAST());
      ExpressionUtils.processExpression(unit, statement.getExpression(), statementType);
    }
  }

  private static void stubForExpressionStatement(CompilationUnit unit) throws IOException {
    List<ASTNode> statements = getNodes(unit, ASTNode.EXPRESSION_STATEMENT);
    for(ASTNode ast : statements){
      ExpressionStatement statement = (ExpressionStatement) ast;
      if (statement.getExpression() instanceof Assignment) {
        Assignment assignment = (Assignment) statement.getExpression();
        Type returnType = TypeUtils.getAppropriateType(unit, statement, assignment);
        ExpressionUtils.processExpression(unit, assignment.getRightHandSide(), returnType);
      }
      Type type = TypeUtils.extractType(statement, statement.getAST());
      ExpressionUtils.processExpression(unit, statement.getExpression(), type);
    }
  }

  public static void stubForVariableDeclaration(CompilationUnit unit) {
    try {
      List<ASTNode> variableDeclarations = getNodes(unit, ASTNode.VARIABLE_DECLARATION_STATEMENT);
      for (ASTNode node : variableDeclarations) {
        VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
        List<VariableDeclarationFragment> fragments = statement.fragments();
        for (VariableDeclarationFragment flag : fragments) {
          Expression initializer = flag.getInitializer();
          Type type = TypeUtils.extractType(statement, statement.getAST());
          ExpressionUtils.processExpression(unit, initializer, type);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void processClassCreation(CompilationUnit unit, Type type, Expression initializer) throws IOException {
    processTypeParameter(unit, type);
    CompilationUnit templateClass = ClassUtils.getTemplateClassFromExpression(unit, initializer);
    if (templateClass == null) {
      return;
    }
    ClassInstanceCreation instance = (ClassInstanceCreation) initializer;
    ClassInstanceCreationUtils.processInstanceCreation(unit, templateClass, instance, type);
    ClassUtils.filter(unit, templateClass);
  }

  private static void processTypeParameter(CompilationUnit unit, Type type) throws IOException {
    if (type.isParameterizedType()) {
      ParameterizedType paramType = (ParameterizedType) type;
      List<ASTNode> args = (List<ASTNode>) paramType.typeArguments();
      for (ASTNode arg : args) {
        Type argType = (Type) arg;
        CompilationUnit paramTemplateClass = ClassUtils.getTemplateClass(unit, argType);
        if (paramTemplateClass == null) {
          continue;
        }
        if (!ClassUtils.isJavaUtil(paramTemplateClass)) {
          List<Type> genericParamTypes = TypeUtils.createGenericParamTypes(argType);
          ClassUtils.addTypeParameterToClass(genericParamTypes, unit, paramTemplateClass);
          ClassUtils.filter(unit, paramTemplateClass);
        }
      }
    }
  }

  private static void processImportStatement(CompilationUnit unit) throws IOException {
    for (ASTNode importStm : getNodes(unit, ASTNode.IMPORT_DECLARATION)) {
      if (!ClassUtils.isJavaUtil(importStm)){
        String typeStr = getClassNameFromImport(importStm);
        String pkg = "temp/" + typeStr.replaceAll("\\.", "/") + ".java";
        if (!(new File(pkg).exists())) {
          Tuple<String, String> inner = InnerClassUtils.getInnerClassImport(importStm);
          if (inner == null) {
            String className = NameUtils.extractSimpleName(typeStr);
            Type type = ImportUtils.getTypeFromImport(className, importStm.getAST(), importStm);
            ClassUtils.getTemplateClassFromExpression(unit, type);
          } else {
            processInnerClass(unit, inner, importStm);
          }
        }
      }
    }
  }

  private static String getClassNameFromImport(ASTNode importStm) {
    return importStm.toString().substring(7, importStm.toString().length() - 2);
  }

  public static void processInnerClass(CompilationUnit unit, Tuple<String, String> inner, String pkgStr) throws IOException {
    Type type = TypeUtils.createType(unit.getAST(), pkgStr, inner.getItem1());
    CompilationUnit templateInner = ClassUtils.getTemplateClass(unit, type);
    Type typeInner = TypeUtils.createType(unit.getAST(), pkgStr, inner.getItem2());
    CompilationUnit declaration = ClassUtils.getTemplateClass(unit, typeInner);
    InnerClassUtils.getTypeDeclarationIfNeeded(inner.getItem2(), ClassUtils.getTypeDeclaration(templateInner), declaration);
  }

  public static void processInnerClass(CompilationUnit unit, Tuple<String, String> inner, ASTNode importStm) throws IOException {
    String pkgStr = importStm.toString().substring(7, importStm.toString().indexOf(inner.getItem1())-1);
    processInnerClass(unit, inner, pkgStr);
  }

  private static void addImportStatement(CompilationUnit unit) throws IOException {
    List<CompilationUnit> classes = new ArrayList<>(ClassRepository.getInstance().getGenerated());
    for (CompilationUnit compilationUnit : classes) {
      TypeDeclaration typeDeclaration = ClassUtils.getTypeDeclaration(compilationUnit);
      List<ASTNode> imports = getNodes(unit, ASTNode.IMPORT_DECLARATION);
      List importsUnit = compilationUnit.imports();
      importsUnit.clear();
      for (ASTNode importStm : imports) {
        String importStr = importStm.toString().trim();
        String classNameStr = typeDeclaration.getName().toString().trim();
        if (!(importStr.endsWith(classNameStr + ";") || importStr.contains(classNameStr + "."))) {
          ImportDeclaration importDeclaration = (ImportDeclaration) importStm;
          importDeclaration = (ImportDeclaration) ASTNode.copySubtree(compilationUnit.getAST(), importDeclaration);
          importsUnit.add(importDeclaration);
        }
      }
      ClassUtils.writeClass(unit,compilationUnit);
    }
    System.out.println(classes.size());
  }

  public static Type processMethodInvocation(CompilationUnit unit, Type type, ASTNode initializer) throws IOException {
    MethodInvocation invocation = (MethodInvocation) initializer;
    if (!(invocation.getExpression() instanceof MethodInvocation)) {
      if (invocation.getExpression() == null) {
        return type;
      }
      CompilationUnit templateClass = ClassUtils.getTemplateClassFromExpression(unit, invocation.getExpression());
      if (templateClass == null) {
        return type;
      }
      Tuple<String, String> tuple = InnerClassUtils.getInnerClassImport(type.toString());
      if (tuple != null) {
        Type t1 = ImportUtils.getTypeBasedOnImports(unit, tuple.getItem1());
        Type t2 = ImportUtils.getTypeBasedOnImports(unit, tuple.getItem2());
        Type actualT1 = ImportUtils.getTypeNotOnImport(unit.getAST(), t1);
        Type actualT2 = ImportUtils.getTypeNotOnImport(unit.getAST(), t2);
        if (!ClassUtils.isJavaLang(actualT1)) {
          CompilationUnit classT1 = ClassUtils.getTemplateClass(unit, actualT1);
          CompilationUnit classT2 = ClassUtils.getTemplateClass(unit, actualT2);
          TypeDeclaration typeDeclaration1 = ClassUtils.getTypeDeclaration(classT1);
          String className = ClassUtils.getTypeDeclaration(classT2).getName().toString();
          InnerClassUtils.getTypeDeclarationIfNeeded(className, typeDeclaration1, classT2);
        }
      }
      return MethodDeclarationUtils.addMethodBasedOnMethodInvocation(unit, type, invocation, templateClass);
    }
    else {
      if (!type.isPrimitiveType()) {
        CompilationUnit templateSuper = ClassUtils.getTemplateClass(unit, type);
        if (templateSuper != null) {
          createClassForType(unit, templateSuper, type);
        }
      }
      CompilationUnit templateClass = SyntheticClassUtils.createSyntheticClass(unit);
      List<Type> argTypes = ParameterUtils.getArgTypes(unit, invocation, invocation.arguments());
      MethodDeclaration duplicate = ParameterUtils.findMethod(templateClass, argTypes, invocation.getName().toString());
      if (duplicate != null && !type.toString().equals(duplicate.getReturnType2().toString())) {
        TypeDeclaration typeDeclaration = ClassUtils.getTypeDeclaration(templateClass);
        SimpleName simpleName = unit.getAST().newSimpleName(typeDeclaration.getName().toString() + 1);
        Type syntheticType = SyntheticClassUtils.getSyntheticType(unit.getAST(), simpleName);
        templateClass = ClassUtils.getTemplateClass(unit, syntheticType);
      }
      type = MethodDeclarationUtils.addMethodBasedOnMethodInvocation(unit, type, invocation, templateClass);
      MethodInvocationUtils.processMethodInvocationChain(unit, invocation, templateClass);
      return type;
    }
  }

  private static void createClassForType(CompilationUnit unit, CompilationUnit templateClass, Type type) throws IOException {
    type = (Type) ASTNode.copySubtree(type.getAST(), type);
    List<Type> genericParamTypes = TypeUtils.createGenericParamTypes(type);
    String baseName = NameUtils.extractSimpleName(type);
    ASTNode imp = ImportUtils.findImport(unit, baseName);
    Type packageType = ImportUtils.getTypeFromImport(baseName, type.getAST(), imp);
    ClassUtils.createClassDeclaration(templateClass, baseName, packageType);
    ClassUtils.addTypeParameterToClass(genericParamTypes, unit, templateClass);
  }

  public static List<ASTNode> getNodes(CompilationUnit unit, int importDeclaration) {
    KindNodeMatcher matcher = new KindNodeMatcher(importDeclaration);
    NodeMatchCalculator calculator = new NodeMatchCalculator(matcher);
    return calculator.getNodes(unit);
  }

  public static List<ASTNode> getNodes(CompilationUnit unit, Class clazz) {
    InstanceNodeMatcher matcher = new InstanceNodeMatcher(clazz);
    NodeMatchCalculator calculator = new NodeMatchCalculator(matcher);
    return calculator.getNodes(unit);
  }
}
