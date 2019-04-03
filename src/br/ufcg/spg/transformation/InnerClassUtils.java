package br.ufcg.spg.transformation;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.refaster.ClassUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InnerClassUtils {
  public static Tuple<String, String> getInnerClassImport(ASTNode importStm) {
    return getInnerClassImport(importStm.toString());
  }

  public static Tuple<String, String> getInnerClassImport(String importStm) {
    String pattern = "([A-Z][A-Za-z]*)\\.([A-Z][A-Za-z]*)";
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(importStm);
    if (m.find()) {
      if (!importStm.contains("java.util")) {
        return new Tuple<>(m.group(1), m.group(2));
      }
    }
    return null;
  }

  public static TypeDeclaration getTypeDeclarationIfNeeded(String className, TypeDeclaration outerTypeDeclaration, CompilationUnit innerClass) {
    List<ASTNode> nodes = outerTypeDeclaration.bodyDeclarations();
    TypeDeclaration declaration = null;
    for (ASTNode node : nodes) {
      if (node instanceof TypeDeclaration) {
        if (((TypeDeclaration) node).getName().toString().equals(className)) {
          declaration = (TypeDeclaration) node;
        }
      }
    }
    if (declaration == null) {
      declaration = ClassUtils.getTypeDeclaration(innerClass);
      declaration = (TypeDeclaration) ASTNode.copySubtree(outerTypeDeclaration.getAST(), declaration);
      outerTypeDeclaration.bodyDeclarations().add(declaration);
    }
    return declaration;
  }
}
