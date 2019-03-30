package br.ufcg.spg.stub;

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
            if (!(invocation.getExpression() instanceof MethodInvocation)) {
              CompilationUnit templateClass = ClassUtils.getTemplateClassBasedOnInvocation(unit, invocation.getExpression());
              if (templateClass.getPackage().toString().contains("java.util")) {
                System.out.println("Class from java.util, we do not need to create a new class.");
                continue;
              }
              MethodDeclarationUtils.addMethodBasedOnMethodInvocation(unit, type, invocation, templateClass);
            }
            else {
              CompilationUnit templateSuper = ClassUtils.getTemplateClass(unit, type);
              createClassForType(unit, templateSuper, type);
              JDTElementUtils.saveClass(templateSuper, ClassUtils.getTypeDeclaration(templateSuper));
              System.out.println("Processing method chain: ");
              CompilationUnit templateClass = SyntheticClassUtils.createSyntheticClass(unit);
              MethodDeclarationUtils.addMethodBasedOnMethodInvocation(unit, type, invocation, templateClass);
              MethodInvocationStub.processMethodInvocationChain(unit, invocation, templateClass);
            }
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
                  List<Type> genericParamTypes = TypeUtils.createGenericParamTypes(argType);
                  TypeDeclaration argClass = ClassUtils.getTypeDeclaration(paramTemplateClass);
                  ClassUtils.addTypeParameterToClass(genericParamTypes, unit, paramTemplateClass);
                  ClassUtils.filterMethods(paramTemplateClass);
                  JDTElementUtils.saveClass(paramTemplateClass, argClass);
                }
              }
            }
            CompilationUnit templateClass = ClassUtils.getTemplateClassBasedOnInvocation(unit, initializer);
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
            FieldDeclarationUtils.processFieldDeclaration(unit, inv, initializer);
          }
        }
      }
      for (ASTNode importStm : getNodes(unit, ASTNode.IMPORT_DECLARATION)) {
        if (!importStm.toString().contains("java.util")){
          String pkg = importStm.toString().substring(7, importStm.toString().length() - 2);
          pkg = "temp/" + pkg.replaceAll("\\.", "/") + ".java";
          if (!(new File(pkg).exists())) {
            String typeStr = importStm.toString().substring(7, importStm.toString().length() - 2);
            typeStr = JDTElementUtils.extractSimpleName(typeStr);
            Type type = ImportUtils.getTypeFromImport(typeStr, importStm.getAST(), importStm);
            CompilationUnit impClass = ClassUtils.getTemplateClassBasedOnInvocation(unit, type);
            JDTElementUtils.saveClass(impClass, ClassUtils.getTypeDeclaration(impClass));
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void createClassForType(CompilationUnit unit, CompilationUnit templateClass, Type type) {
    type = (Type) ASTNode.copySubtree(type.getAST(), type);
    List<Type> genericParamTypes = TypeUtils.createGenericParamTypes(type);
    String baseName = JDTElementUtils.extractSimpleName(type);
    ASTNode imp = ImportUtils.findImport(unit, baseName);
    System.out.println("The import is: " + baseName);
    Type packageType = ImportUtils.getTypeFromImport(baseName, type.getAST(), imp);
    ClassUtils.createClassDeclaration(templateClass, baseName, packageType);
    ClassUtils.addTypeParameterToClass(genericParamTypes, unit, templateClass);
  }

  public static List<ASTNode> getNodes(CompilationUnit unit, int importDeclaration) {
    KindNodeMatcher matcher = new KindNodeMatcher(importDeclaration);
    NodeMatchCalculator calculator = new NodeMatchCalculator(matcher);
    return calculator.getNodes(unit);
  }
}
