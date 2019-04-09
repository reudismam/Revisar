package br.ufcg.spg.transformation;

import br.ufcg.spg.type.TypeUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.util.List;

public class ClassInstanceCreationUtils {

  public static void processInstanceCreation(CompilationUnit unit, CompilationUnit templateClass,
                                             Expression initializer, Type statementType) throws IOException {
    ClassInstanceCreation instanceCreation = (ClassInstanceCreation) initializer;
    AST ast = templateClass.getAST();
    Type classType = TypeUtils.extractType(instanceCreation, ast);
    String typeStr = NameUtils.extractSimpleName(classType);
    SimpleName name = ast.newSimpleName(typeStr);
    MethodInvocationUtils.processMethodInvocation(unit, null, templateClass, name, null, instanceCreation.arguments(), false, true);
    processSuperClass(unit, ast, classType, templateClass, statementType);
  }

  private static void processSuperClass(CompilationUnit unit,
                                        AST ast, Type classType, CompilationUnit classUnit, Type leftHandSideClass) throws IOException {
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(classUnit);
    String rightHandSideName = NameUtils.extractSimpleName(classType);
    String leftHandSideName = NameUtils.extractSimpleName(leftHandSideClass);
    CompilationUnit baseClass = ClassUtils.getTemplateClass(unit, leftHandSideClass);
    if (baseClass == null) {
      return;
    }
    List<Type> genericParamTypes = TypeUtils.createGenericParamTypes(leftHandSideClass);
    ClassUtils.addTypeParameterToClass(genericParamTypes, unit, baseClass);
    leftHandSideClass = (Type) ASTNode.copySubtree(ast, leftHandSideClass);
    if (!leftHandSideName.equals(rightHandSideName)) {
      classDecl.setSuperclassType(leftHandSideClass);
    }
    ClassUtils.addTypeParameterToClass(genericParamTypes, unit, classUnit);
  }
}
