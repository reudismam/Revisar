package br.ufcg.spg.stub;

import br.ufcg.spg.refaster.ClassUtils;
import br.ufcg.spg.transformation.JDTElementUtils;
import br.ufcg.spg.type.TypeUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.util.List;

public class ClassInstanceCreationStub {
  public static void stub(CompilationUnit unit, CompilationUnit templateClass,
                          Expression initializer, Type statementType) throws IOException {
    ClassInstanceCreation instanceCreation = (ClassInstanceCreation) initializer;
    AST ast = templateClass.getAST();
    Type classType = TypeUtils.extractType(instanceCreation, ast);
    String typeStr = JDTElementUtils.extractSimpleName(classType);
    SimpleName name = ast.newSimpleName(typeStr);
    Type type = MethodInvocationStub.stub(unit, null, templateClass, name, null, instanceCreation.arguments(), false, true);
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
    ClassUtils.addTypeParameterToClass(genericParamTypes, unit, baseClass);
    leftHandSideClass = (Type) ASTNode.copySubtree(ast, leftHandSideClass);
    if (!leftHandSideName.equals(rightHandSideName)) {
      classDecl.setSuperclassType(leftHandSideClass);
    }
    ClassUtils.addTypeParameterToClass(genericParamTypes, unit, classUnit);
  }
}
