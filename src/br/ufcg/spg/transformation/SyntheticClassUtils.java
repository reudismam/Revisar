package br.ufcg.spg.transformation;

import br.ufcg.spg.refaster.ClassUtils;
import br.ufcg.spg.stub.StubUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.util.List;

public class SyntheticClassUtils {
  public static int classNumber = 1;

  public static CompilationUnit createSyntheticClass(CompilationUnit unit) throws IOException {
    String className = "Class" + classNumber;
    SimpleName simpleName = unit.getAST().newSimpleName(className);
    Type qualifiedType = getSyntheticType(unit.getAST(), simpleName);
    System.out.println("[Synthetic Class]: Qualified type is: \n" + qualifiedType);
    CompilationUnit templateClass = ClassUtils.getTemplateClass(unit, qualifiedType);
    List<ASTNode> importDeclarationList = StubUtils.getNodes(unit, ASTNode.IMPORT_DECLARATION);
    templateClass.imports().clear();
    for (ASTNode node : importDeclarationList) {
      ImportDeclaration importDeclaration = (ImportDeclaration) node;
      importDeclaration = (ImportDeclaration) ASTNode.copySubtree(templateClass.getAST(), importDeclaration);
      templateClass.imports().add(importDeclaration);
    }
    return templateClass;
  }

  public static Type getSyntheticType(AST ast, SimpleName simpleName) {
    Name name = ast.newName("syntethic");
    simpleName =  (SimpleName) ASTNode.copySubtree(ast, simpleName);
    return ast.newNameQualifiedType(name, simpleName);
  }

  public static Type getSyntheticType(AST ast) {
    SimpleName name = ast.newSimpleName("Class" + classNumber);
    return getSyntheticType(ast, name);
  }
}
