package br.ufcg.spg.refaster;

import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.stub.StubUtils;
import br.ufcg.spg.transformation.ImportUtils;
import br.ufcg.spg.transformation.JDTElementUtils;
import br.ufcg.spg.transformation.MethodDeclarationUtils;
import br.ufcg.spg.type.TypeUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassUtils {
  public static TypeDeclaration createClassDeclaration(CompilationUnit unit, String baseName, Type packageType) {
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(unit);
    AST ast = classDecl.getAST();
    SimpleName simpleName = ast.newSimpleName(baseName);
    JDTElementUtils.setName(classDecl, simpleName);
    PackageDeclaration declaration = ast.newPackageDeclaration();
    declaration.setName(ast.newName(packageType.toString().substring(0, packageType.toString().lastIndexOf("."))));
    unit.setPackage(declaration);
    return classDecl;
  }

  public static TypeDeclaration getTypeDeclaration(CompilationUnit cUnit) {
    final TypeDeclaration typeDecl = (TypeDeclaration) cUnit.types().get(0);
    return typeDecl;
  }

  public static void addTypeParameterToClass(List<Type> paramTypes, TypeDeclaration classDecl) {
    if (classDecl.typeParameters().size() != paramTypes.size()) {
      for (Type type : paramTypes) {
        SimpleType simpleType = (SimpleType) ASTNode.copySubtree(classDecl.getAST(), type);
        TypeParameter parameter = classDecl.getAST().newTypeParameter();
        SimpleName simpleName = (SimpleName) simpleType.getName();
        simpleName = (SimpleName) ASTNode.copySubtree(parameter.getAST(), simpleName);
        parameter.setName(simpleName);
        classDecl.typeParameters().add(parameter);
      }
    }
  }

  public static void addConstructor(CompilationUnit unit, CompilationUnit templateClass,
                                                 ClassInstanceCreation invocation, AST ast, String typeStr) {
    MethodDeclaration mDecl = templateClass.getAST().newMethodDeclaration();
    mDecl.setConstructor(true);
    MethodDeclarationUtils.addBody(templateClass, mDecl);
    MethodDeclarationUtils.addThrowStatement(mDecl);
    SimpleName methodName = ast.newSimpleName(typeStr);
    MethodDeclarationUtils.setName(mDecl, methodName);
    MethodDeclarationUtils.addModifier(mDecl, Modifier.ModifierKeyword.PUBLIC_KEYWORD);
    List<ASTNode> arguments = (List<ASTNode>) invocation.arguments();
    mDecl = ParameterUtils.addParameters(unit, arguments, templateClass, mDecl);
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    classDecl.bodyDeclarations().add(mDecl);
  }

  public static void filterMethods(CompilationUnit templateClass) {
    Map<String, MethodDeclaration> map = new HashMap<>();
    List<ASTNode> declarations = StubUtils.getNodes(templateClass, ASTNode.METHOD_DECLARATION);
    for (ASTNode node : declarations) {
      MethodDeclaration declaration = (MethodDeclaration) node;
      if (!map.containsKey(declaration.toString())) {
        map.put(declaration.toString(), declaration);
      }
    }
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    classDecl.bodyDeclarations().clear();
    for (Map.Entry<String, MethodDeclaration> entry : map.entrySet()) {
      classDecl.bodyDeclarations().add(entry.getValue());
    }
  }

  public static CompilationUnit getTemplateClass(CompilationUnit unit, Type classType) throws IOException {
    CompilationUnit templateClass;
    AST ast = unit.getAST();
    boolean isStatic = classType.toString().equals("void");
    if (isStatic) {
      String typeName = JDTElementUtils.extractSimpleName(classType.toString());
      classType = ImportUtils.getTypeBasedOnImports(unit, typeName);
    }
    Type packageType;
    String baseName = JDTElementUtils.extractSimpleName(classType);
    if (baseName == null) {
      throw new RuntimeException("Could not find a type for " + baseName);
    }
    ASTNode imp = ImportUtils.findImport(unit, baseName);
    if (classType.isNameQualifiedType()) {
      packageType = classType;
    }
    else {
      packageType = ImportUtils.getTypeFromImport(baseName, ast, imp);
    }
    String pkg = "temp/" + packageType.toString().replaceAll("\\.", "/") + ".java";
    if ((new File(pkg).exists())) {
      templateClass = JParser.parseFromFile(pkg);
    }
    else {
      templateClass = createNewClass(baseName, packageType);
    }
    return templateClass;
  }

  private static CompilationUnit createNewClass(String baseName, Type imp) throws IOException {
    CompilationUnit templateClass;
    templateClass = JParser.parseFromFile(TemplateConstants.ClassPath);
    createClassDeclaration(templateClass, baseName, imp);
    return templateClass;
  }

  public static CompilationUnit getTemplateClassBasedOnInvocation(CompilationUnit unit, Expression expression, AST ast2) throws IOException {
    Type invExpressionType = TypeUtils.extractType(expression, ast2);
    return getTemplateClass(unit, invExpressionType);
  }
}
