package br.ufcg.spg.stub;

import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.refaster.ParameterUtils;
import br.ufcg.spg.refaster.TemplateConstants;
import br.ufcg.spg.refaster.config.ClassUtils;
import br.ufcg.spg.transformation.JDTElementUtils;
import br.ufcg.spg.transformation.MethodDeclarationUtils;
import br.ufcg.spg.type.TypeUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClassInstanceCreationStub {
  public static void stubForMethodInvocation(CompilationUnit unit, CompilationUnit templateClass,
                                             Expression initializer, VariableDeclarationStatement statement) throws IOException {
    ClassInstanceCreation invocation = (ClassInstanceCreation) initializer;
    AST ast = templateClass.getAST();
    Type classType = TypeUtils.extractType(invocation, ast);
    String typeStr = JDTElementUtils.extractSimpleName(classType);
    //ASTNode imp = StubUtils.findImport(unit, typeStr);
    //TypeDeclaration classDecl = ClassUtils.createClassDeclaration(templateClass, typeStr, imp);
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    System.out.println("Before\n");
    System.out.println(templateClass.toString());
    MethodDeclaration mDecl = templateClass.getAST().newMethodDeclaration();
    mDecl.setConstructor(true);
    MethodDeclarationUtils.addBody(templateClass, mDecl);
    MethodDeclarationUtils.addThrowStatement(mDecl);
    System.out.println(templateClass);
    SimpleName methodName = ast.newSimpleName(typeStr);
    MethodDeclarationUtils.setName(mDecl, methodName);
    MethodDeclarationUtils.addModifier(mDecl, Modifier.ModifierKeyword.PUBLIC_KEYWORD);
    List<ASTNode> arguments = (List<ASTNode>) invocation.arguments();
    mDecl = ParameterUtils.addParameters(unit, initializer, arguments, templateClass, classDecl, mDecl);
    System.out.println("After\n");
    classDecl.bodyDeclarations().add(mDecl);
    Type statementType = TypeUtils.extractType(statement, ast);
    processSuperClass(unit, statement, ast, classType, classDecl, statementType);
    System.out.println(templateClass.toString());
    JDTElementUtils.saveClass(templateClass, classDecl);
  }

  private static void processSuperClass(CompilationUnit unit, VariableDeclarationStatement statement,
                                        AST ast, Type classType, TypeDeclaration classDecl, Type statementType) throws IOException {
    ASTNode imp;
    if (!classType.toString().equals(statementType.toString())) {
      CompilationUnit baseClass = JParser.parseFromFile(TemplateConstants.ClassPath);
      Type leftHandSideClass = TypeUtils.extractType(statement, ast);
      List<Type> genericParamTypes = createGenericParamTypes(ast, leftHandSideClass);
      String baseName = JDTElementUtils.extractSimpleName(leftHandSideClass);
      imp = StubUtils.findImport(unit, baseName);
      TypeDeclaration superDecl = ClassUtils.createClassDeclaration(baseClass, baseName, imp);
      addTypeParameterToClass(genericParamTypes, superDecl);
      String pkg = baseClass.getPackage().getName().toString().replaceAll("\\.", "/");
      System.out.println("Super Class: \n" + baseClass);
      leftHandSideClass = (Type) ASTNode.copySubtree(ast, leftHandSideClass);
      classDecl.setSuperclassType(leftHandSideClass);
      addTypeParameterToClass(genericParamTypes, classDecl);
      FileUtils.write(new File("temp/" + pkg + "/" + superDecl.getName() + ".java"), baseClass.toString());
    }
  }

  private static List<Type> createGenericParamTypes(AST ast, Type leftHandSideClass) {
    List<Type> genericParamTypes = new ArrayList<>();
    if (leftHandSideClass.isParameterizedType()) {
      ParameterizedType paramType = (ParameterizedType) leftHandSideClass;
      List<Type> typeArguments = paramType.typeArguments();
      char letter = 'T';
      for (Type type : typeArguments) {
        type = (Type) ASTNode.copySubtree(ast, type);
        Name name = ast.newName(String.valueOf(letter ++));
        SimpleType simpleType = (SimpleType) type;
        simpleType.setName(name);
        genericParamTypes.add(simpleType);
      }
      paramType.typeArguments().clear();
      for (Type type : genericParamTypes) {
        type = (Type) ASTNode.copySubtree(paramType.getAST(), type);
        typeArguments.add(type);
      }
    }
    return genericParamTypes;
  }

  private static void addTypeParameterToClass(List<Type> paramTypes, TypeDeclaration superDecl) {
    for (Type type : paramTypes) {
      SimpleType simpleType = (SimpleType) ASTNode.copySubtree(superDecl.getAST(), type);
      TypeParameter parameter = superDecl.getAST().newTypeParameter();
      SimpleName simpleName = (SimpleName) simpleType.getName();
      simpleName = (SimpleName) ASTNode.copySubtree(parameter.getAST(), simpleName);
      parameter.setName(simpleName);
      superDecl.typeParameters().add(parameter);
    }
  }
}
