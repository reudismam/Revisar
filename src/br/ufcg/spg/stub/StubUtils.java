package br.ufcg.spg.stub;

import br.ufcg.spg.matcher.KindNodeMatcher;
import br.ufcg.spg.matcher.calculator.NodeMatchCalculator;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.refaster.ClassUtils;
import br.ufcg.spg.transformation.FieldDeclarationUtils;
import br.ufcg.spg.transformation.JDTElementUtils;
import br.ufcg.spg.transformation.MethodDeclarationUtils;
import br.ufcg.spg.transformation.SyntheticClassUtils;
import br.ufcg.spg.type.TypeUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class StubUtils {

  public static void generateStubsForClass(String classFile) {
    try {
      FileUtils.cleanDirectory(new File("temp/"));
      CompilationUnit unit = JParser.parseFromFile(classFile);
      FileUtils.writeStringToFile(new File("temp/defaultpkg/temp.java"), unit.getRoot().toString());
      List<ASTNode> invocations = getNodes(unit, ASTNode.VARIABLE_DECLARATION_STATEMENT);
      System.out.println(unit.toString());
      for(ASTNode ast : invocations){
        VariableDeclarationStatement inv = (VariableDeclarationStatement) ast;
        System.out.println(inv + " : " + TypeUtils.extractType(inv, inv.getAST()));
      }
      for (ASTNode node : invocations) {
        VariableDeclarationStatement inv = (VariableDeclarationStatement) node;
        List<VariableDeclarationFragment> fragments = inv.fragments();
        for (VariableDeclarationFragment flag : fragments) {
          Expression initializer = flag.getInitializer();
          if (initializer instanceof MethodInvocation) {
            MethodInvocation invocation = (MethodInvocation) initializer;
            Type type = TypeUtils.extractType(inv, inv.getAST());
            CompilationUnit templateClass = ClassUtils.getTemplateClassBasedOnInvocation(unit, invocation.getExpression(), invocation.getAST());
            if (templateClass.getPackage().toString().contains("java.util")) {
              System.out.println("Class from java.util, we do not need to create a new class.");
              continue;
            }
            MethodDeclarationUtils.addMethodBasedOnMethodInvocation(unit, type, invocation, templateClass);
          }
          else if (initializer instanceof ClassInstanceCreation) {
            Type type = TypeUtils.extractType(inv, inv.getAST());
            if (type.isParameterizedType()) {
              ParameterizedType paramType = (ParameterizedType) type;
              List<ASTNode> args = paramType.typeArguments();
              for (ASTNode arg : args) {
                Type argType = (Type) arg;
                CompilationUnit paramTemplateClass = ClassUtils.getTemplateClass(unit, argType);
                if (!paramTemplateClass.getPackage().toString().contains("java.util")) {
                  List<Type> genericParamTypes = TypeUtils.createGenericParamTypes(arg.getAST(), argType);
                  TypeDeclaration argClass = ClassUtils.getTypeDeclaration(paramTemplateClass);
                  ClassUtils.addTypeParameterToClass(genericParamTypes, argClass);
                  ClassUtils.filterMethods(paramTemplateClass);
                  JDTElementUtils.saveClass(paramTemplateClass, argClass);
                }
              }
            }
            CompilationUnit templateClass = ClassUtils.getTemplateClassBasedOnInvocation(unit, initializer, initializer.getAST());
            if (templateClass.getPackage().toString().contains("java.util")) {
              continue;
            }
            System.out.println("Class provided to class instance creation: \n" + templateClass);
            ClassInstanceCreation instance = (ClassInstanceCreation) initializer;
            ClassInstanceCreationStub.stub(unit, templateClass, instance, inv);
            ClassUtils.filterMethods(templateClass);
            TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
            JDTElementUtils.saveClass(templateClass, classDecl);
          }
          else if (initializer instanceof  FieldAccess) {
            processFieldDeclaration(unit, inv, initializer);
          }
        }
      }
      for (ASTNode importStm : getNodes(unit, ASTNode.IMPORT_DECLARATION)) {
        if (!importStm.toString().contains("java.util")){
          String pkg = importStm.toString().substring(7, importStm.toString().length() - 2);
          pkg = "temp/" + pkg.replaceAll("\\.", "/") + ".java";
          if (!(new File(pkg).exists())) {
            CompilationUnit impClass = ClassUtils.getTemplateClassBasedOnInvocation(unit, (Expression) importStm, importStm.getAST());
            JDTElementUtils.saveClass(impClass, ClassUtils.getTypeDeclaration(impClass));
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void processFieldDeclaration(CompilationUnit unit, VariableDeclarationStatement inv, Expression initializer) throws IOException {
    System.out.println("General Type: " + TypeUtils.extractType(initializer, initializer.getAST()));
    FieldAccess facces = (FieldAccess) initializer;
    Expression expression = facces.getExpression();
    System.out.println(facces.getName());
    if (expression instanceof MethodInvocation) {
      MethodInvocation methodInvocation = (MethodInvocation) expression;
      if (TypeUtils.extractType(expression, unit.getAST()).toString().equals("void")) {
        CompilationUnit templateSynt = SyntheticClassUtils.createSyntheticClass(unit);
        VariableDeclarationFragment vfrag = unit.getAST().newVariableDeclarationFragment();
        SimpleName fieldName = (SimpleName) ASTNode.copySubtree(vfrag.getAST(), facces.getName());
        vfrag.setName(fieldName);
        FieldDeclaration fieldDeclaration = unit.getAST().newFieldDeclaration(vfrag);
        FieldDeclarationUtils.addModifier(fieldDeclaration, Modifier.ModifierKeyword.PUBLIC_KEYWORD);
        Type type = TypeUtils.extractType(inv, inv.getAST());
        type = (Type) ASTNode.copySubtree(type.getAST(), type);
        fieldDeclaration.setType(type);
        TypeDeclaration typeDeclaration = ClassUtils.getTypeDeclaration(templateSynt);
        fieldDeclaration = (FieldDeclaration) ASTNode.copySubtree(typeDeclaration.getAST(), fieldDeclaration);
        typeDeclaration.bodyDeclarations().add(fieldDeclaration);
        CompilationUnit templateChain = SyntheticClassUtils.createSyntheticClass(unit);
        Type returnType = SyntheticClassUtils.getSyntheticType(unit, typeDeclaration.getName());
        MethodInvocationStub.stub(unit, templateChain, methodInvocation.getName(), returnType, methodInvocation.arguments(), false);
        if (methodInvocation.getExpression() instanceof MethodInvocation) {
          MethodInvocation chain = (MethodInvocation) methodInvocation.getExpression();
          while (chain.getExpression() instanceof  MethodInvocation) {
            MethodInvocationStub.stub(unit, templateChain, chain.getName(), returnType, chain.arguments(), false);
            chain = (MethodInvocation) chain.getExpression();
          }
          System.out.println(templateChain);
          CompilationUnit templateClass = ClassUtils.getTemplateClassBasedOnInvocation(unit, chain.getExpression(), unit.getAST());
          MethodDeclarationUtils.addMethodBasedOnMethodInvocation(unit, returnType, chain, templateClass);
          System.out.println("The final template class is: \n" + templateClass);
          JDTElementUtils.saveClass(templateClass, ClassUtils.getTypeDeclaration(templateChain));
        }
        System.out.println(methodInvocation.getExpression());
        System.out.println(templateChain);
        JDTElementUtils.saveClass(templateChain, ClassUtils.getTypeDeclaration(templateChain));
      }
    }
    throw new UnsupportedOperationException();
  }

  public static List<ASTNode> getNodes(CompilationUnit unit, int importDeclaration) {
    KindNodeMatcher matcher = new KindNodeMatcher(importDeclaration);
    NodeMatchCalculator calculator = new NodeMatchCalculator(matcher);
    return calculator.getNodes(unit);
  }
}
