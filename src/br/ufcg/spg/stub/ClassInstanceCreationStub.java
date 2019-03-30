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
                          Expression initializer, VariableDeclarationStatement statement) throws IOException {
    ClassInstanceCreation invocation = (ClassInstanceCreation) initializer;
    AST ast = templateClass.getAST();
    Type classType = TypeUtils.extractType(invocation, ast);
    String typeStr = JDTElementUtils.extractSimpleName(classType);
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    ClassUtils.addConstructor(unit, templateClass, invocation, ast, typeStr);
    Type statementType = TypeUtils.extractType(statement, ast);
    processSuperClass(unit, statement, ast, classType, templateClass, statementType);
  }

  private static void processSuperClass(CompilationUnit unit, VariableDeclarationStatement statement,
                                        AST ast, Type classType, CompilationUnit classUnit, Type statementType) throws IOException {
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(classUnit);
    String rightHandSideName = JDTElementUtils.extractSimpleName(classType);
    String leftHandSideName = JDTElementUtils.extractSimpleName(statementType);
    Type type = TypeUtils.extractType(statement, statement.getAST());
    CompilationUnit baseClass = ClassUtils.getTemplateClass(unit, type);
    Type leftHandSideClass = TypeUtils.extractType(statement, ast);
    List<Type> genericParamTypes = TypeUtils.createGenericParamTypes(leftHandSideClass);
    String baseName = JDTElementUtils.extractSimpleName(leftHandSideClass);
    ASTNode imp = ImportUtils.findImport(unit, baseName);
    Type packageType = ImportUtils.getTypeFromImport(baseName, ast, imp);
    TypeDeclaration superDecl = ClassUtils.createClassDeclaration(baseClass, baseName, packageType);
    ClassUtils.addTypeParameterToClass(genericParamTypes, unit, baseClass);
    String pkg = baseClass.getPackage().getName().toString().replaceAll("\\.", "/");
    System.out.println("Super Class: \n" + baseClass);
    leftHandSideClass = (Type) ASTNode.copySubtree(ast, leftHandSideClass);
    if (!leftHandSideName.equals(rightHandSideName)) {
      classDecl.setSuperclassType(leftHandSideClass);
    }
    ClassUtils.addTypeParameterToClass(genericParamTypes, unit, classUnit);
    FileUtils.write(new File("temp/" + pkg + "/" + superDecl.getName() + ".java"), baseClass.toString());
  }
}
