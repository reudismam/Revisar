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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StubUtils {

  public static void generateStubsForClass(String classFile) throws IOException {
    FileUtils.cleanDirectory(new File("temp/"));
    stubsForVariableDeclaration(classFile);
    stubForExpressionStatement(classFile);
    stubForThrowStatement(classFile);
    stubForIfStatement(classFile);
    stubForParameterizedType(classFile);
    CompilationUnit unit = stubForInfixExpression(classFile);
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
      if (infixExpression.getLeftOperand() instanceof MethodInvocation) {
        if (infixExpression.getOperator().equals(InfixExpression.Operator.EQUALS)) {
           Type type = SyntheticClassUtils.getSyntheticType(unit.getAST());
           System.out.println("Left hand: " + infixExpression.getLeftOperand());
           System.out.println(infixExpression);
           processMethodInvocation(unit, type, infixExpression.getLeftOperand());
        }
      }
    }
    return unit;
  }

  private static void stubForParameterizedType(String classFile) throws IOException {
    CompilationUnit unit = JParser.parseFromFile(classFile);
    FileUtils.writeStringToFile(new File("temp/defaultpkg/temp.java"), unit.getRoot().toString());
    List<ASTNode> nodes = getNodes(unit, ASTNode.PARAMETERIZED_TYPE);
    for (ASTNode node : nodes) {
      System.out.println("node: " + node + " : " + TypeUtils.extractType(node, node.getAST()));
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
        System.out.println();
        PrefixExpression infixExpression = (PrefixExpression) statement.getExpression();
        System.out.println("If statement contains a method invocation");
        if (infixExpression.getOperand() instanceof MethodInvocation) {
          System.out.println("operand: " + infixExpression.getOperand());
          MethodInvocation invocation = (MethodInvocation) infixExpression.getOperand();
          processMethodInvocation(unit, unit.getAST().newPrimitiveType(PrimitiveType.BOOLEAN), invocation);
        }
      }
      else if (statement.getExpression() instanceof MethodInvocation) {
        MethodInvocation invocation = (MethodInvocation) statement.getExpression();
        processMethodInvocation(unit, unit.getAST().newPrimitiveType(PrimitiveType.BOOLEAN), invocation);
      }
      System.out.println("if stm: " + statement.getExpression() + " : " + statement.getExpression().getClass());
      //throw new RuntimeException();
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
        JDTElementUtils.saveClass(templateClass);
      }
      System.out.println(statement.getExpression() + " : " + TypeUtils.extractType(statement.getExpression(), statement.getAST()));
    }
    return unit;
  }

  private static CompilationUnit stubForExpressionStatement(String classFile) throws IOException {
    CompilationUnit unit = JParser.parseFromFile(classFile);
    FileUtils.writeStringToFile(new File("temp/defaultpkg/temp.java"), unit.getRoot().toString());
    List<ASTNode> invocations = getNodes(unit, ASTNode.EXPRESSION_STATEMENT);
    for(ASTNode ast : invocations){
      ExpressionStatement invocation = (ExpressionStatement) ast;
      System.out.println(invocation.getExpression() + " : " + TypeUtils.extractType(invocation.getExpression(), invocation.getAST()));
      if (invocation.getExpression() instanceof Assignment) {
        System.out.println("Class instance creation\n");
        Assignment assignment = (Assignment) invocation.getExpression();
        System.out.println(assignment + " : " + TypeUtils.extractType(assignment.getLeftHandSide(), invocation.getAST()));
        if (assignment.getRightHandSide() instanceof MethodInvocation) {
          Type type = TypeUtils.extractType(assignment.getLeftHandSide(), invocation.getAST());
          /*if (assignment.getRightHandSide().toString().contains("SamplePruner")){
            System.out.println(assignment.getRightHandSide() + " : " + assignment.getRightHandSide().getClass());
            System.out.println(type);
            throw new RuntimeException();
          }*/
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
            System.out.println("invocation: " + invocation);
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
      for(ASTNode ast : variableDeclarations){
        VariableDeclarationStatement variableDeclaration = (VariableDeclarationStatement) ast;
        System.out.println(variableDeclaration + " : " + TypeUtils.extractType(variableDeclaration, variableDeclaration.getAST()));
      }
      for (ASTNode node : variableDeclarations) {
        VariableDeclarationStatement inv = (VariableDeclarationStatement) node;
        System.out.println("The variable declaration is: " + inv);
        List<VariableDeclarationFragment> fragments = inv.fragments();
        for (VariableDeclarationFragment flag : fragments) {
          Expression initializer = flag.getInitializer();
          if (initializer instanceof MethodInvocation) {
            Type type = TypeUtils.extractType(inv, inv.getAST());
            processMethodInvocation(unit, type, initializer);
          }
          else if (initializer instanceof ClassInstanceCreation) {
            Type type = TypeUtils.extractType(inv, inv.getAST());
            processClassCreation(unit, type, initializer);
          }
          else if (initializer instanceof  FieldAccess) {
            FieldDeclarationUtils.processFieldDeclaration(unit, inv, initializer);
          }
        }
      }
      System.out.println("Finished!!");
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
    System.out.println("Class provided to class instance creation: \n" + templateClass);
    ClassInstanceCreation instance = (ClassInstanceCreation) initializer;
    ClassInstanceCreationStub.stub(unit, templateClass, instance, type);
    ClassUtils.filter(templateClass);
    JDTElementUtils.saveClass(templateClass);
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
          ClassUtils.filter(paramTemplateClass);
          JDTElementUtils.saveClass(paramTemplateClass);
        }
      }
    }
  }

  private static void processImportStatement(CompilationUnit unit) throws IOException {
    for (ASTNode importStm : getNodes(unit, ASTNode.IMPORT_DECLARATION)) {
      if (!importStm.toString().contains("java.util")){
        String pkg = importStm.toString().substring(7, importStm.toString().length() - 2);
        pkg = "temp/" + pkg.replaceAll("\\.", "/") + ".java";
        System.out.println("Creating new class: " + pkg);
        if (!(new File(pkg).exists())) {
          Tuple<String, String> inner = getInnerClassImport(importStm);
          if (inner == null) {
            String typeStr = importStm.toString().substring(7, importStm.toString().length() - 2);
            typeStr = JDTElementUtils.extractSimpleName(typeStr);
            Type type = ImportUtils.getTypeFromImport(typeStr, importStm.getAST(), importStm);
            CompilationUnit impClass = ClassUtils.getTemplateClassBasedOnInvocation(unit, type);
            JDTElementUtils.saveClass(impClass);
          } else {
            String className = inner.getItem1();
            String pkgStr = importStm.toString().substring(7, importStm.toString().indexOf(className)-1);
            Type type = TypeUtils.createType(unit, pkgStr, className);
            CompilationUnit templateInner = ClassUtils.getTemplateClass(unit, type);
            Type typeInner = TypeUtils.createType(unit, pkgStr, inner.getItem2());
            CompilationUnit templateInside = ClassUtils.getTemplateClass(unit, typeInner);
            TypeDeclaration typeDeclaration = ClassUtils.getTypeDeclaration(templateInside);
            ClassRepository.getInstance().getGenerated().remove(templateInside);
            typeDeclaration = (TypeDeclaration) ASTNode.copySubtree(templateInner.getAST(), typeDeclaration);
            TypeDeclaration outerTypeDeclaration = ClassUtils.getTypeDeclaration(templateInner);
            outerTypeDeclaration.bodyDeclarations().add(typeDeclaration);
            JDTElementUtils.saveClass(templateInner);
            System.out.println("the new class created is:\n" + templateInner);
          }
        }
      }
    }
  }

  private static void addImportStatement(CompilationUnit unit) throws IOException {
    List<CompilationUnit> classes = new ArrayList<>(ClassRepository.getInstance().getGenerated());
    System.out.println("Classes generated:");
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
      JDTElementUtils.saveClass(compilationUnit);
    }
  }

  public static void processMethodInvocation(CompilationUnit unit, Type type, ASTNode initializer) throws IOException {
    MethodInvocation invocation = (MethodInvocation) initializer;
    if (!(invocation.getExpression() instanceof MethodInvocation)) {
      if (invocation.getExpression() == null) {
        return;
      }
      CompilationUnit templateClass = ClassUtils.getTemplateClassBasedOnInvocation(unit, invocation.getExpression());
      /*if (initializer.toString().contains("SamplePruner.")){
        System.out.println(initializer + " : " + initializer.getClass());
        System.out.println(((MethodInvocation) initializer).getExpression());
        System.out.println(templateClass);
        throw new RuntimeException();
      }*/
      if (templateClass == null) {
        System.out.println("Class from java.util, we do not need to create a new class.");
        return;
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
        JDTElementUtils.saveClass(templateSuper);
      }
      CompilationUnit templateClass = SyntheticClassUtils.createSyntheticClass(unit);
      MethodDeclarationUtils.addMethodBasedOnMethodInvocation(unit, type, invocation, templateClass);
      MethodInvocationStub.processMethodInvocationChain(unit, invocation, templateClass);
      JDTElementUtils.saveClass(templateClass);
    }
  }

  private static Tuple<String, String> getInnerClassImport(ASTNode importStm) {
    String pattern = "([A-Z][A-Za-z]*)\\.([A-Z][A-Za-z]*)";
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(importStm.toString());
    if (m.find()) {
      if (!importStm.toString().contains("java.util")) {
        return new Tuple<>(m.group(1), m.group(2));
      }
    }
    return null;
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
