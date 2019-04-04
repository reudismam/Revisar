package br.ufcg.spg.stub;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.matcher.InstanceNodeMatcher;
import br.ufcg.spg.matcher.KindNodeMatcher;
import br.ufcg.spg.matcher.calculator.NodeMatchCalculator;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.refaster.ClassUtils;
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
    CompilationUnit unit = JParser.parseFromFile(classFile);
    FileUtils.writeStringToFile(new File("temp/defaultpkg/temp.java"), unit.getRoot().toString());
    List<ASTNode> invocations = getNodes(unit, ASTNode.CONDITIONAL_EXPRESSION);
    for(ASTNode ast : invocations) {
      ConditionalExpression conditionalExpression = (ConditionalExpression) ast;
      Expression expression = conditionalExpression.getThenExpression();
      if (expression instanceof MethodInvocation) {
        MethodInvocation invocation = (MethodInvocation) expression;
      }
    }
    FileUtils.cleanDirectory(new File("temp/"));
    stubsForVariableDeclaration(classFile);
    stubForExpressionStatement(classFile);
    stubForThrowStatement(classFile);
    stubForIfStatement(classFile);
    stubForParameterizedType(classFile);
    stubForInfixExpression(classFile);
    processImportStatement(unit);
    addImportStatement(unit);
    System.out.println("Finished");
  }

  private static CompilationUnit stubForInfixExpression(String classFile) throws IOException {
    CompilationUnit unit = JParser.parseFromFile(classFile);
    FileUtils.writeStringToFile(new File("temp/defaultpkg/temp.java"), unit.getRoot().toString());
    List<ASTNode> invocations = getNodes(unit, ASTNode.INFIX_EXPRESSION);
    for(ASTNode ast : invocations) {
      InfixExpression infixExpression = (InfixExpression) ast;
      InfixExpression.Operator operator = infixExpression.getOperator();
      if (operator.equals(InfixExpression.Operator.EQUALS)
        || (operator.equals(InfixExpression.Operator.NOT_EQUALS))) {
        Type type = SyntheticClassUtils.getSyntheticType(unit.getAST());
        processLeftAndRight(unit, infixExpression, type);
      } else if (operator.equals(InfixExpression.Operator.CONDITIONAL_AND)
        || (operator.equals(InfixExpression.Operator.CONDITIONAL_OR))) {
        Type type = unit.getAST().newPrimitiveType(PrimitiveType.BOOLEAN);
        processLeftAndRight(unit, infixExpression, type);
      }
    }
    return unit;
  }

  private static void processLeftAndRight(CompilationUnit unit, InfixExpression infixExpression, Type returnType) throws IOException {
    if (infixExpression.getLeftOperand() instanceof MethodInvocation) {
       processMethodInvocation(unit, returnType, infixExpression.getLeftOperand());
    } else if (infixExpression.getRightOperand() instanceof  MethodInvocation) {
      processMethodInvocation(unit, returnType, infixExpression.getLeftOperand());
    }
  }

  private static void stubForParameterizedType(String classFile) throws IOException {
    CompilationUnit unit = JParser.parseFromFile(classFile);
    FileUtils.writeStringToFile(new File("temp/defaultpkg/temp.java"), unit.getRoot().toString());
    List<ASTNode> nodes = getNodes(unit, ASTNode.PARAMETERIZED_TYPE);
    for (ASTNode node : nodes) {
      processTypeParameter(unit, TypeUtils.extractType(node, node.getAST()));
    }
  }

  private static void stubForIfStatement(String classFile) throws IOException {
    CompilationUnit unit = JParser.parseFromFile(classFile);
    FileUtils.writeStringToFile(new File("temp/defaultpkg/temp.java"), unit.getRoot().toString());
    List<ASTNode> invocations = getNodes(unit, ASTNode.IF_STATEMENT);
    for(ASTNode ast : invocations) {
      IfStatement statement = (IfStatement) ast;
      if (statement.getExpression() instanceof PrefixExpression) {
        PrefixExpression infixExpression = (PrefixExpression) statement.getExpression();
        if (infixExpression.getOperand() instanceof MethodInvocation) {
          MethodInvocation invocation = (MethodInvocation) infixExpression.getOperand();
          processMethodInvocation(unit, unit.getAST().newPrimitiveType(PrimitiveType.BOOLEAN), invocation);
        }
      }
      else if (statement.getExpression() instanceof MethodInvocation) {
        MethodInvocation invocation = (MethodInvocation) statement.getExpression();
        processMethodInvocation(unit, unit.getAST().newPrimitiveType(PrimitiveType.BOOLEAN), invocation);
      }
    }
  }

  private static CompilationUnit stubForThrowStatement(String classFile) throws IOException {
    CompilationUnit unit = JParser.parseFromFile(classFile);
    FileUtils.writeStringToFile(new File("temp/defaultpkg/temp.java"), unit.getRoot().toString());
    List<ASTNode> invocations = getNodes(unit, ASTNode.THROW_STATEMENT);
    for(ASTNode ast : invocations) {
      ThrowStatement statement = (ThrowStatement) ast;
      if (statement.getExpression() instanceof ClassInstanceCreation) {
        String className = JDTElementUtils.extractSimpleName(TypeUtils.extractType(
                statement.getExpression(), statement.getAST()));
        Type classType = ImportUtils.getTypeBasedOnImports(unit, className);
        CompilationUnit templateClass = ClassUtils.getTemplateClass(unit, classType);
        ClassInstanceCreation instance = (ClassInstanceCreation) statement.getExpression();
        Type statementType = TypeUtils.extractType(statement.getExpression(), statement.getAST());
        ClassInstanceCreationStub.stub(unit, templateClass, instance, statementType);
        JDTElementUtils.saveClass(unit, templateClass);
      }
    }
    return unit;
  }

  private static CompilationUnit stubForExpressionStatement(String classFile) throws IOException {
    CompilationUnit unit = JParser.parseFromFile(classFile);
    FileUtils.writeStringToFile(new File("temp/defaultpkg/temp.java"), unit.getRoot().toString());
    List<ASTNode> invocations = getNodes(unit, ASTNode.EXPRESSION_STATEMENT);
    for(ASTNode ast : invocations){
      ExpressionStatement invocation = (ExpressionStatement) ast;
      if (invocation.getExpression() instanceof Assignment) {
        Assignment assignment = (Assignment) invocation.getExpression();
        if (assignment.getRightHandSide() instanceof MethodInvocation) {
          Type type = TypeUtils.extractType(assignment.getLeftHandSide(), invocation.getAST());
          if (type.isPrimitiveType()) {
            processMethodInvocation(unit, type, assignment.getRightHandSide());
          }
          else if (type.isArrayType()) {
            ArrayType arrayType = (ArrayType) type;
            String simpleName = JDTElementUtils.extractSimpleName(type);
            if (simpleName.contains("[")) {
              simpleName = simpleName.substring(0, simpleName.indexOf("["));
            }
            type = ImportUtils.getTypeBasedOnImports(unit, simpleName);
            arrayType.setElementType(type);
            processMethodInvocation(unit, arrayType, assignment.getRightHandSide());
          }
          else {
            String importedName = JDTElementUtils.extractSimpleName(type);
            Type fromImport = ImportUtils.getTypeBasedOnImports(unit, importedName);
            processMethodInvocation(unit, fromImport, assignment.getRightHandSide());
          }
        }
        else if (assignment.getRightHandSide() instanceof ClassInstanceCreation) {
          Type type = TypeUtils.extractType(assignment.getLeftHandSide(), assignment.getAST());
          if (!type.toString().equals(SyntheticClassUtils.getSyntheticType(unit.getAST()).toString())) {
            String className = JDTElementUtils.extractSimpleName(type);
            type = ImportUtils.getTypeBasedOnImports(unit, className);
          }
          processClassCreation(unit, type, assignment.getRightHandSide());
        }
      }
      else if (invocation.getExpression() instanceof MethodInvocation) {
        MethodInvocation invo = (MethodInvocation) invocation.getExpression();
        processMethodInvocation(unit, TypeUtils.extractType(invocation, invocation.getAST()), invo);
      }
      else if (invocation.getExpression() instanceof ClassInstanceCreation) {
        throw new RuntimeException();
      }
    }
    return unit;
  }

  public static void stubsForVariableDeclaration(String classFile) {
    try {
      CompilationUnit unit = JParser.parseFromFile(classFile);
      FileUtils.writeStringToFile(new File("temp/defaultpkg/temp.java"), unit.getRoot().toString());
      List<ASTNode> variableDeclarations = getNodes(unit, ASTNode.VARIABLE_DECLARATION_STATEMENT);
      for (ASTNode node : variableDeclarations) {
        VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
        List<VariableDeclarationFragment> fragments = statement.fragments();
        for (VariableDeclarationFragment flag : fragments) {
          Expression initializer = flag.getInitializer();
          Type type = TypeUtils.extractType(statement, statement.getAST());
          if (initializer instanceof MethodInvocation) {
            processMethodInvocation(unit, type, initializer);
          }
          else if (initializer instanceof ClassInstanceCreation) {
            processClassCreation(unit, type, initializer);
          }
          else if (initializer instanceof  FieldAccess) {
            FieldDeclarationUtils.processFieldDeclaration(unit, type, initializer);
          }
          else if (initializer instanceof ConditionalExpression) {
            ConditionalExpression conditionalExpression  = (ConditionalExpression) initializer;
            if (conditionalExpression.getThenExpression() instanceof MethodInvocation) {
              processMethodInvocation(unit, type, conditionalExpression.getThenExpression());
            }
            else if (conditionalExpression.getElseExpression() instanceof MethodInvocation) {
              processMethodInvocation(unit, type, conditionalExpression.getElseExpression());
            }
            //System.out.println(conditionalExpression);
            //throw  new RuntimeException();
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void processClassCreation(CompilationUnit unit, Type type, Expression initializer) throws IOException {
    processTypeParameter(unit, type);
    CompilationUnit templateClass = ClassUtils.getTemplateClassBasedOnInvocation(unit, initializer);
    if (templateClass == null) {
      return;
    }
    ClassInstanceCreation instance = (ClassInstanceCreation) initializer;
    ClassInstanceCreationStub.stub(unit, templateClass, instance, type);
    ClassUtils.filter(unit, templateClass);
    JDTElementUtils.saveClass(unit, templateClass);
  }

  private static void processTypeParameter(CompilationUnit unit, Type type) throws IOException {
    if (type.isParameterizedType()) {
      ParameterizedType paramType = (ParameterizedType) type;
      List<ASTNode> args = paramType.typeArguments();
      for (ASTNode arg : args) {
        Type argType = (Type) arg;
        CompilationUnit paramTemplateClass = ClassUtils.getTemplateClass(unit, argType);
        if (paramTemplateClass == null) {
          continue;
        }
        if (!paramTemplateClass.getPackage().toString().contains("java.util")) {
          List<Type> genericParamTypes = TypeUtils.createGenericParamTypes(argType);
          ClassUtils.addTypeParameterToClass(genericParamTypes, unit, paramTemplateClass);
          ClassUtils.filter(unit, paramTemplateClass);
          JDTElementUtils.saveClass(unit, paramTemplateClass);
        }
      }
    }
  }

  private static void processImportStatement(CompilationUnit unit) throws IOException {
    for (ASTNode importStm : getNodes(unit, ASTNode.IMPORT_DECLARATION)) {
      if (!importStm.toString().contains("java.util")){
        String pkg = importStm.toString().substring(7, importStm.toString().length() - 2);
        pkg = "temp/" + pkg.replaceAll("\\.", "/") + ".java";
        if (!(new File(pkg).exists())) {
          Tuple<String, String> inner = InnerClassUtils.getInnerClassImport(importStm);
          if (inner == null) {
            String typeStr = importStm.toString().substring(7, importStm.toString().length() - 2);
            typeStr = JDTElementUtils.extractSimpleName(typeStr);
            Type type = ImportUtils.getTypeFromImport(typeStr, importStm.getAST(), importStm);
            CompilationUnit impClass = ClassUtils.getTemplateClassBasedOnInvocation(unit, type);
            JDTElementUtils.saveClass(unit, impClass);
          } else {
            processInnerClass(unit, inner, importStm);
          }
        }
      }
    }
  }

  public static void processInnerClass(CompilationUnit unit, Tuple<String, String> inner, String pkgStr) throws IOException {
    Type type = TypeUtils.createType(unit.getAST(), pkgStr, inner.getItem1());
    CompilationUnit templateInner = ClassUtils.getTemplateClass(unit, type);
    Type typeInner = TypeUtils.createType(unit.getAST(), pkgStr, inner.getItem2());
    CompilationUnit declaration = ClassUtils.getTemplateClass(unit, typeInner);
    InnerClassUtils.getTypeDeclarationIfNeeded(inner.getItem2(), ClassUtils.getTypeDeclaration(templateInner), declaration);
    JDTElementUtils.saveClass(unit, templateInner);
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
        if (importStm.toString().trim().matches("")) {
          System.out.println("This import statement contains inner class." + importStm);
        }
        String importStr = importStm.toString().trim();
        String classNameStr = typeDeclaration.getName().toString().trim();
        if (!(importStr.endsWith(classNameStr + ";") || importStr.contains(classNameStr + "."))) {
          ImportDeclaration importDeclaration = (ImportDeclaration) importStm;
          importDeclaration = (ImportDeclaration) ASTNode.copySubtree(compilationUnit.getAST(), importDeclaration);
          importsUnit.add(importDeclaration);
        }
      }
      JDTElementUtils.writeClass(unit,compilationUnit);
    }
    System.out.println(classes.size());
  }

  public static void processMethodInvocation(CompilationUnit unit, Type type, ASTNode initializer) throws IOException {
    MethodInvocation invocation = (MethodInvocation) initializer;
    if (!(invocation.getExpression() instanceof MethodInvocation)) {
      if (invocation.getExpression() == null) {
        return;
      }
      CompilationUnit templateClass = ClassUtils.getTemplateClassBasedOnInvocation(unit, invocation.getExpression());
      if (templateClass == null) {
        return;
      }
      Tuple<String, String> tuple = InnerClassUtils.getInnerClassImport(type.toString());
      if (tuple != null) {
        Type t1 = ImportUtils.getTypeBasedOnImports(unit, tuple.getItem1());
        Type t2 = ImportUtils.getTypeBasedOnImports(unit, tuple.getItem2());
        Type actualT1 = ImportUtils.getTypeNotOnImport(unit.getAST(), t1);
        Type actualT2 = ImportUtils.getTypeNotOnImport(unit.getAST(), t2);
        if (!actualT1.toString().contains("java.lang")) {
          CompilationUnit classT1 = ClassUtils.getTemplateClass(unit, actualT1);
          CompilationUnit classT2 = ClassUtils.getTemplateClass(unit, actualT2);
          TypeDeclaration typeDeclaration1 = ClassUtils.getTypeDeclaration(classT1);
          String className = ClassUtils.getTypeDeclaration(classT2).getName().toString();
          InnerClassUtils.getTypeDeclarationIfNeeded(className, typeDeclaration1, classT2);
          //typeDeclaration2 = (TypeDeclaration) ASTNode.copySubtree(typeDeclaration1.getAST(), typeDeclaration2);
          //typeDeclaration1.bodyDeclarations().add(typeDeclaration2);
          System.out.println(classT1);
          JDTElementUtils.saveClass(unit, classT1);
        }
      }
      MethodDeclarationUtils.addMethodBasedOnMethodInvocation(unit, type, invocation, templateClass);
    }
    else {
      if (!type.isPrimitiveType()) {
        CompilationUnit templateSuper = ClassUtils.getTemplateClass(unit, type);
        if (templateSuper == null) {
          return;
        }
        createClassForType(unit, templateSuper, type);
        JDTElementUtils.saveClass(unit, templateSuper);
      }
      CompilationUnit templateClass = SyntheticClassUtils.createSyntheticClass(unit);
      MethodDeclarationUtils.addMethodBasedOnMethodInvocation(unit, type, invocation, templateClass);
      MethodInvocationStub.processMethodInvocationChain(unit, invocation, templateClass);
      JDTElementUtils.saveClass(unit, templateClass);
    }
  }

  private static void createClassForType(CompilationUnit unit, CompilationUnit templateClass, Type type) throws IOException {
    type = (Type) ASTNode.copySubtree(type.getAST(), type);
    List<Type> genericParamTypes = TypeUtils.createGenericParamTypes(type);
    String baseName = JDTElementUtils.extractSimpleName(type);
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
