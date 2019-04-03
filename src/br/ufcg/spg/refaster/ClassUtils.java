package br.ufcg.spg.refaster;

import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.transformation.ClassRepository;
import br.ufcg.spg.transformation.ImportUtils;
import br.ufcg.spg.transformation.JDTElementUtils;
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
    System.out.println(cUnit.types().get(0));
    final TypeDeclaration typeDecl = (TypeDeclaration) cUnit.types().get(0);
    return typeDecl;
  }

  public static void addTypeParameterToClass(List<Type> paramTypes, CompilationUnit unit, CompilationUnit templateClass) throws IOException {
    //try {
      TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
      if (classDecl.typeParameters().size() != paramTypes.size()) {
        for (Type type : paramTypes) {
          SimpleType simpleType = (SimpleType) ASTNode.copySubtree(classDecl.getAST(), type);
          TypeParameter parameter = classDecl.getAST().newTypeParameter();
          SimpleName simpleName = (SimpleName) simpleType.getName();
          simpleName = (SimpleName) ASTNode.copySubtree(parameter.getAST(), simpleName);
          parameter.setName(simpleName);
          classDecl.typeParameters().add(parameter);
          if (type instanceof ParameterizedType) {
            CompilationUnit paramTemplateClass = ClassUtils.getTemplateClass(unit, type);
            List<Type> genericParamTypes = TypeUtils.createGenericParamTypes(type);
            addTypeParameterToClass(genericParamTypes, unit, paramTemplateClass);
            JDTElementUtils.saveClass(paramTemplateClass);
            System.out.println("The type is: " + type);
            throw new RuntimeException();
          }
      }
    }
      //JDTElementUtils.saveClass(templatClass, classDecl);
    /*} catch (IOException e) {
      e.printStackTrace();
    }*/
  }

  /*public static void addConstructor(CompilationUnit unit, CompilationUnit templateClass,
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
  }*/

  public static void filter(CompilationUnit templateClass) {
    Map<String, ASTNode> map = new HashMap<>();
    List<ASTNode> declarations = ClassUtils.getTypeDeclaration(templateClass).bodyDeclarations();//StubUtils.getNodes(templateClass, ASTNode.METHOD_DECLARATION);
    for (ASTNode node : declarations) {
      ASTNode declaration = node;
      if (!map.containsKey(declaration.toString())) {
        map.put(declaration.toString(), declaration);
      }
    }
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    classDecl.bodyDeclarations().clear();
    for (Map.Entry<String, ASTNode> entry : map.entrySet()) {
      classDecl.bodyDeclarations().add(entry.getValue());
    }
  }

  public static CompilationUnit getTemplateClass(CompilationUnit unit, Type classType) throws IOException {
    CompilationUnit templateClass;
    AST ast = unit.getAST();
    if (classType.toString().equals("void")) {
      return null;
    }
    classType = TypeUtils.getClassType(unit, classType);
    Type packageType;
    if (classType.isWildcardType()) {
      WildcardType wildcardType = (WildcardType) classType;
      classType = wildcardType.getBound();
    }
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
    CompilationUnit clazz = ClassRepository.getInstance().getClassInRepository(packageType.toString());
    if (clazz != null) {
      return clazz;
    }
    String pkg = "temp/" + packageType.toString().replaceAll("\\.", "/") + ".java";
    System.out.println("The package of the type is: " + pkg);
    if ((new File(pkg).exists())) {
      templateClass = JParser.parseFromFile(pkg);
      throw new RuntimeException();
    }
    else {
      if (baseName.equals("Class1")) {
        if (cont++ == 1) throw new RuntimeException();
        System.out.println("Creating a new class of SamplePruner: " + baseName + ":" + packageType);
      }
      templateClass = createNewClass(baseName, packageType);
      ClassRepository.getInstance().add(templateClass);
    }
    String pkgStr = templateClass.getPackage().toString();
    if (pkgStr.contains("java.lang") || pkgStr.contains("java.util")) {
      return null;
    }
    if (baseName.endsWith("Exception")) {
      System.out.println("Creating a new exception:\n" + baseName);
      TypeDeclaration typeDeclaration = getTypeDeclaration(templateClass);
      Type type = TypeUtils.createType(templateClass.getAST(), "java.lang", "RuntimeException");
      typeDeclaration.setSuperclassType(type);
    }
    return templateClass;
  }

  static int cont = 0;

  private static CompilationUnit createNewClass(String baseName, Type imp) throws IOException {
    CompilationUnit templateClass;
    templateClass = JParser.parseFromFile(TemplateConstants.ClassPath);
    createClassDeclaration(templateClass, baseName, imp);
    return templateClass;
  }

  public static CompilationUnit getTemplateClassBasedOnInvocation(CompilationUnit unit, ASTNode expression) throws IOException {
    Type invExpressionType = TypeUtils.getClassType(unit, expression);
    invExpressionType = ImportUtils.getTypeNotOnImport(unit.getAST(), invExpressionType);
    CompilationUnit templateClass = getTemplateClass(unit, invExpressionType);
    if (templateClass == null) {
      return null;
    }
    return templateClass;
  }

}
