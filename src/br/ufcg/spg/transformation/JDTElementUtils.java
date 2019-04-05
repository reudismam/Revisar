package br.ufcg.spg.transformation;

import br.ufcg.spg.refaster.ClassUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;

public class JDTElementUtils {
  public static void setName(TypeDeclaration mDecl, SimpleName name) {
    AST ast = mDecl.getAST();
    name = (SimpleName) ASTNode.copySubtree(ast, name);
    mDecl.setName(name);
  }

  public static void saveClass(CompilationUnit unit, CompilationUnit templateClass) {
    //ClassUtils.filter(unit, templateClass);
  }

  public static void writeClass(CompilationUnit unit, CompilationUnit templateClass) throws IOException {
    ClassUtils.filter(unit, templateClass);
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    if (!templateClass.getPackage().toString().contains("java.util")) {
      String pkg = templateClass.getPackage().getName().toString().replaceAll("\\.", "/");
      FileUtils.write(new File("temp/" + pkg + "/" + classDecl.getName() + ".java"), templateClass.toString());
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
