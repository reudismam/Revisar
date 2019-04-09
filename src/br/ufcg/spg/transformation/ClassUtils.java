package br.ufcg.spg.transformation;

import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.refaster.TemplateConstants;
import br.ufcg.spg.type.TypeUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassUtils {

  public static TypeDeclaration createClassDeclaration(CompilationUnit unit, String baseName, Type packageType) {
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(unit);
    AST ast = classDecl.getAST();
    SimpleName simpleName = ast.newSimpleName(baseName);
    setName(classDecl, simpleName);
    PackageDeclaration declaration = ast.newPackageDeclaration();
    declaration.setName(ast.newName(packageType.toString().substring(0, packageType.toString().lastIndexOf("."))));
    unit.setPackage(declaration);
    return classDecl;
  }

  public static TypeDeclaration getTypeDeclaration(CompilationUnit cUnit) {
    final TypeDeclaration typeDecl = (TypeDeclaration) cUnit.types().get(0);
    return typeDecl;
  }

  public static void addTypeParameterToClass(List<Type> paramTypes, CompilationUnit unit, CompilationUnit templateClass) throws IOException {
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
          throw new RuntimeException();
        }
      }
    }
  }

  public static void filter(CompilationUnit unit, CompilationUnit templateClass) {
    if (ClassUtils.isJavaLang(templateClass) || ClassUtils.isJavaUtil(templateClass)) {
      return;
    }
    String name = ClassUtils.getTypeDeclaration(templateClass).getName().toString();
    SimpleName simpleName = unit.getAST().newSimpleName(name);
    if (name.contains("Exception")){
      processException(unit, templateClass, simpleName);
    }
    filterTypeDeclaration(ClassUtils.getTypeDeclaration(templateClass));
  }

  private static void filterTypeDeclaration(TypeDeclaration classDecl) {
    Map<String, ASTNode> map = new HashMap<>();
    List<ASTNode> declarations = classDecl.bodyDeclarations();
    for (ASTNode node : declarations) {
      ASTNode declaration = node;
      if (!map.containsKey(declaration.toString())) {
        map.put(declaration.toString(), declaration);
      }
    }
    classDecl.bodyDeclarations().clear();
    for (Map.Entry<String, ASTNode> entry : map.entrySet()) {
      if (entry.getValue() instanceof TypeDeclaration) {
        filterTypeDeclaration((TypeDeclaration) entry.getValue());
      }
      classDecl.bodyDeclarations().add(entry.getValue());
    }
    Map<String, List<MethodDeclaration>> methodInvocationMap = new HashMap<>();
    declarations = (List<ASTNode>) classDecl.bodyDeclarations();
    for (ASTNode node : declarations) {
      if (node instanceof MethodDeclaration) {
        MethodDeclaration declaration = (MethodDeclaration) node;
        String methodName = declaration.getName().toString() + declaration.parameters();
        if (!methodInvocationMap.containsKey(methodName)) {
          methodInvocationMap.put(methodName, new ArrayList<>());
        }
        methodInvocationMap.get(methodName).add(declaration);
      }
    }
    for (Map.Entry<String, List<MethodDeclaration>> entry : methodInvocationMap.entrySet()) {
      if (entry.getValue().size() > 1) {
        List<MethodDeclaration> toRemove = new ArrayList<>();
        for (int i = 0; i < entry.getValue().size(); i++) {
          MethodDeclaration astNode = entry.getValue().get(i);
          System.out.println("Return type:\n" + astNode);
          if (astNode.getReturnType2().toString().contains("syntethic")) {
            toRemove.add(astNode);
          }
        }
        if (!toRemove.isEmpty()) {
          System.out.println("Before: \n" + classDecl);
          for (MethodDeclaration astNode : toRemove) {
            //declarations.remove(astNode);
          }
          System.out.println("After: \n" + classDecl);
          //if (true) throw new RuntimeException();
        }
      }
    }
  }

  private static void processException(CompilationUnit unit, CompilationUnit templateClass, SimpleName simpleName) {
    try {
      Class<?> clazz = Class.forName("java.lang.Exception");
      Constructor<?>[] list = clazz.getDeclaredConstructors();
      for (Constructor<?> constructor : list) {
        List<ASTNode> arguments = new ArrayList<>();
        for (Class<?> parameter : constructor.getParameterTypes()) {
          String parameterStr = parameter.toString().trim();
          Type type = null;
          if (parameterStr.contains("class")) {
            String className = parameterStr.substring(parameterStr.lastIndexOf(".")+1);
            String pckgName = parameterStr.substring(6, parameterStr.lastIndexOf(".") - 1);
            type = TypeUtils.createType(unit.getAST(), pckgName, className);
          }
          else {
            if (parameterStr.equals("boolean")) {
              type = unit.getAST().newPrimitiveType(PrimitiveType.BOOLEAN);
            }
            else if (parameterStr.equals("int")) {
              type = unit.getAST().newPrimitiveType(PrimitiveType.INT);
            }
            else {
              throw new RuntimeException();
            }
          }
          arguments.add(type);
        }
        MethodDeclarationUtils.createMethod(unit, null, templateClass, simpleName, null, arguments, false, true);
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (true) throw new RuntimeException();
    }
  }

  public static void filterNew(CompilationUnit templateClass) {
    Map<String, List<ASTNode>> map = new HashMap<>();
    List<ASTNode> declarations = ClassUtils.getTypeDeclaration(templateClass).bodyDeclarations();
    for (ASTNode node : declarations) {
      String key = null;
      if (node instanceof  MethodDeclaration) {
        key = ((MethodDeclaration) node).getName().toString();
      } else if (node instanceof FieldDeclaration) {
        FieldDeclaration declaration = (FieldDeclaration) node;
        VariableDeclarationFragment frag = (VariableDeclarationFragment) declaration.fragments().get(0);
        key = frag.getName().toString();
      }
      else if (node instanceof TypeDeclaration) {
        key = ((TypeDeclaration) node).getName().toString();
      }
      if (!map.containsKey(key)) {
        List<ASTNode> nodes = new ArrayList<>();
        map.put(key, nodes);
      }
      map.get(key).add(node);
    }
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    classDecl.bodyDeclarations().clear();
    for (Map.Entry<String, List<ASTNode>> entry : map.entrySet()) {
      if (entry.getValue().size() >= 4) {
        for (ASTNode node : entry.getValue()) {
          System.out.println(node);
        }
        if (true) throw new RuntimeException();
      }
      classDecl.bodyDeclarations().add(entry.getValue().get(entry.getValue().size() - 1));
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
    String baseName = NameUtils.extractSimpleName(classType);
    if (baseName == null) {
      throw new RuntimeException("Could not find a type for " + baseName);
    }
    ASTNode imp = ImportUtils.findImport(unit, baseName);
    if (classType.isNameQualifiedType()) {
      packageType = classType;
      if (packageType.toString().contains("SamplePruner.LimitPruneRetStatus.SamplePruner")) {
        throw new RuntimeException();
      }
    }
    else {
      packageType = ImportUtils.getTypeFromImport(baseName, ast, imp);
      if (packageType.toString().contains("SamplePruner.LimitPruneRetStatus.SamplePruner")) {
        throw new RuntimeException();
      }
    }
    CompilationUnit clazz = ClassRepository.getInstance().getClassInRepository(packageType.toString());
    if (clazz != null) {
      return clazz;
    }
    templateClass = createNewClass(baseName, packageType);
    ClassRepository.getInstance().add(templateClass);
    String pkgStr = templateClass.getPackage().toString();
    if (pkgStr.contains("java.lang") || pkgStr.contains("java.util")) {
      return null;
    }
    if (baseName.endsWith("Exception")) {
      TypeDeclaration typeDeclaration = getTypeDeclaration(templateClass);
      Type type = TypeUtils.createType(templateClass.getAST(), "java.lang", "RuntimeException");
      typeDeclaration.setSuperclassType(type);
    }
    if (templateClass.getPackage().getName().toString().contains("SamplePruner.LimitPruneRetStatus.SamplePruner")) {
      throw new RuntimeException();
    }
    return templateClass;
  }

  private static CompilationUnit createNewClass(String baseName, Type imp) throws IOException {
    CompilationUnit templateClass;
    templateClass = JParser.parseFromFile(TemplateConstants.ClassPath);
    createClassDeclaration(templateClass, baseName, imp);
    return templateClass;
  }

  public static CompilationUnit getTemplateClassBasedOnInvocation(CompilationUnit unit, ASTNode expression) throws IOException {
    Type invExpressionType = TypeUtils.extractTypeGlobalAnalysis(unit, expression);
    CompilationUnit templateClass = getTemplateClass(unit, invExpressionType);
    if (templateClass == null) {
      return null;
    }
    return templateClass;
  }

  public static void addModifier(TypeDeclaration classDecl, Modifier.ModifierKeyword modifier) {
    AST ast = classDecl.getAST();
    List<ASTNode> modifiers = classDecl.modifiers();
    boolean containModifers = false;
    for (ASTNode node : modifiers) {
      Modifier modifier1 = (Modifier) node;
      if (modifier1.toString().equals(modifier.toString())) {
        containModifers = true;
        break;
      }
    }
    if (!containModifers) {
      classDecl.modifiers().add(ast.newModifier(modifier));
    }
  }

  public static boolean isJavaLang(CompilationUnit templateClass) {
    return templateClass.getPackage().toString().contains("java.lang");
  }

  public static boolean isJavaUtil(CompilationUnit templateClass) {
    return templateClass.getPackage().toString().contains("java.util");
  }

  public static void setName(TypeDeclaration mDecl, SimpleName name) {
    AST ast = mDecl.getAST();
    name = (SimpleName) ASTNode.copySubtree(ast, name);
    mDecl.setName(name);
  }

  public static void writeClass(CompilationUnit unit, CompilationUnit templateClass) throws IOException {
    filter(unit, templateClass);
    TypeDeclaration classDecl = getTypeDeclaration(templateClass);
    if (!templateClass.getPackage().toString().contains("java.util")) {
      String pkg = templateClass.getPackage().getName().toString().replaceAll("\\.", "/");
      FileUtils.write(new File("temp/" + pkg + "/" + classDecl.getName() + ".java"), templateClass.toString());
    } else {
      System.out.println("From java.util, we do not need to create a class.");
    }
  }
}
