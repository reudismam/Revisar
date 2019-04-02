package br.ufcg.spg.transformation;

import br.ufcg.spg.refaster.ClassUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JDTElementUtils {
  public static void setName(TypeDeclaration mDecl, SimpleName name) {
    AST ast = mDecl.getAST();
    name = (SimpleName) ASTNode.copySubtree(ast, name);
    mDecl.setName(name);
  }

  public static void saveClass(CompilationUnit templateClass) throws IOException {
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    List<CompilationUnit> classes = ClassRepository.getInstance().getGenerated();
    CompilationUnit toRemote = null;
    for (CompilationUnit cunit : classes) {
      TypeDeclaration typeDeclaration = ClassUtils.getTypeDeclaration(cunit);
      if ((cunit.getPackage().toString().trim() + typeDeclaration.getName()).equals(
              templateClass.getPackage().toString().trim() + classDecl.getName())) {
        toRemote = cunit;
      }
    }
    //System.out.println("Number of classes: " + ClassRepository.getInstance().getGenerated().size());
    if (toRemote != null) {
      classes.remove(toRemote);
    }
    ClassUtils.filter(templateClass);
    classes.add(templateClass);
    if (!templateClass.getPackage().toString().contains("java.util")) {
      String pkg = templateClass.getPackage().getName().toString().replaceAll("\\.", "/");
      FileUtils.write(new File("temp/" + pkg + "/" + classDecl.getName() + ".java"), templateClass.toString());
      //System.out.println("class save to " + pkg);
      System.out.println(templateClass);
    } else {
      System.out.println("From java.util, we do not need to create a class.");
    }
  }

  public static String extractSimpleName(Type type) {
    String typeStr = type.toString();
    return extractSimpleName(typeStr);
  }

  public static String extractSimpleName(String typeStr) {
    if (typeStr.contains("<")) {
      typeStr = typeStr.substring(0, typeStr.indexOf("<"));
    }
    if (typeStr.contains(".")) {
      typeStr = typeStr.substring(typeStr.lastIndexOf(".") + 1);
    }
    if (typeStr.contains(";")) {
      typeStr = typeStr.replaceAll(";", "").trim();
    }
    return typeStr;
  }
}
