package br.ufcg.spg.stub;

import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.refaster.TemplateConstants;
import br.ufcg.spg.refaster.ClassUtils;
import br.ufcg.spg.transformation.ImportUtils;
import br.ufcg.spg.transformation.JDTElementUtils;
import br.ufcg.spg.type.TypeUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ClassInstanceCreationStub {
  public static void stub(CompilationUnit unit, CompilationUnit templateClass,
                          Expression initializer, Type statementType) throws IOException {
    ClassInstanceCreation invocation = (ClassInstanceCreation) initializer;
    AST ast = templateClass.getAST();
    Type classType = TypeUtils.extractType(invocation, ast);
    String typeStr = JDTElementUtils.extractSimpleName(classType);
    SimpleName name = ast.newSimpleName(typeStr);
    MethodInvocationStub.stub(unit, templateClass, name, null, invocation.arguments(), false, true);
    processSuperClass(unit, ast, classType, templateClass, statementType);
  }

  private static void processSuperClass(CompilationUnit unit,
                                        AST ast, Type classType, CompilationUnit classUnit, Type leftHandSideClass) throws IOException {
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(classUnit);
    String rightHandSideName = JDTElementUtils.extractSimpleName(classType);
    String leftHandSideName = JDTElementUtils.extractSimpleName(leftHandSideClass);
    CompilationUnit baseClass = ClassUtils.getTemplateClass(unit, leftHandSideClass);
    if (baseClass == null) {
      return;
    }
    List<Type> genericParamTypes = TypeUtils.createGenericParamTypes(leftHandSideClass);
    String baseName = JDTElementUtils.extractSimpleName(leftHandSideClass);
    ASTNode imp = ImportUtils.findImport(unit, baseName);
    Type packageType = ImportUtils.getTypeFromImport(baseName, ast, imp);
    //TypeDeclaration superDecl = ClassUtils.createClassDeclaration(baseClass, baseName, packageType);
    TypeDeclaration superDecl = ClassUtils.getTypeDeclaration(baseClass);//ClassUtils.createClassDeclaration(baseClass, baseName, packageType);
    ClassUtils.addTypeParameterToClass(genericParamTypes, unit, baseClass);
    String pkg = baseClass.getPackage().getName().toString().replaceAll("\\.", "/");
    System.out.println("Super Class: \n" + baseClass);
    leftHandSideClass = (Type) ASTNode.copySubtree(ast, leftHandSideClass);
    if (!leftHandSideName.equals(rightHandSideName)) {
      classDecl.setSuperclassType(leftHandSideClass);
    }
    ClassUtils.addTypeParameterToClass(genericParamTypes, unit, classUnit);
    //FileUtils.write(new File("temp/" + pkg + "/" + superDecl.getName() + ".java"), baseClass.toString());
  }
}
