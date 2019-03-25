package br.ufcg.spg.transformation;

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

  public static void saveClass(CompilationUnit templateClass, TypeDeclaration classDecl) throws IOException {
    String pkg = templateClass.getPackage().getName().toString().replaceAll("\\.", "/");
    FileUtils.write(new File("temp/" + pkg + "/" + classDecl.getName() + ".java"), templateClass.toString());
  }

  public static String extractSimpleName(Type type) {
    String typeStr = type.toString();
    if (typeStr.contains(".")) {
      typeStr = typeStr.substring(typeStr.lastIndexOf(".") + 1);
    }
    if (typeStr.contains("<")) {
      System.out.println(typeStr);
      typeStr = typeStr.substring(0, typeStr.lastIndexOf("<"));
    }
    return typeStr;
  }
}
