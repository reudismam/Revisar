package br.ufcg.spg.transformation;

import br.ufcg.spg.refaster.ClassUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;

public class SyntheticClassUtils {
  public static int classNumber = 1;

  public static CompilationUnit createSyntheticClass(CompilationUnit unit) throws IOException {
    String className = "Class" + classNumber;
    SimpleName simpleName = unit.getAST().newSimpleName(className);
    Type qualifiedType = getSyntheticType(unit, simpleName);
    System.out.println("[Synthetic Class]: Qualified type is: \n" + qualifiedType);
    CompilationUnit templateClass = ClassUtils.getTemplateClass(unit, qualifiedType);
    return templateClass;
  }

  public static Type getSyntheticType(CompilationUnit unit, SimpleName simpleName) {
    AST ast = unit.getAST();
    Name name = ast.newName("syntethic");
    simpleName =  (SimpleName) ASTNode.copySubtree(ast, simpleName);
    return ast.newNameQualifiedType(name, simpleName);
  }
}
