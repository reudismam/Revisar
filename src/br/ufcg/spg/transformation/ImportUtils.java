package br.ufcg.spg.transformation;

import br.ufcg.spg.stub.StubUtils;
import br.ufcg.spg.type.TypeUtils;
import java.util.List;
import org.eclipse.jdt.core.dom.*;

public class ImportUtils {
  public static Type getTypeBasedOnImports(CompilationUnit unit, String className) {
    AST ast = unit.getAST();
    ASTNode imp = findImport(unit, className);
    return getTypeFromImport(className, ast, imp);
  }

  public static Type getTypeFromImport(String classname, AST ast, ASTNode imp) {
    Name name;
    SimpleName sname = ast.newSimpleName(classname);
    if (imp != null) {
      name = ast.newName(imp.toString().substring(7, imp.toString().lastIndexOf(".")));
    }
    else {
      name = ast.newName("java.lang");
    }
    return getTypeNotOnImport(ast, ast.newNameQualifiedType(name, sname));
  }

  public static ASTNode findImport(CompilationUnit unit, String typeName) {
    List<ASTNode> imports = StubUtils.getNodes(unit, ASTNode.IMPORT_DECLARATION);
    for (ASTNode imp : imports) {
      String impStr = imp.toString().trim().substring(0, imp.toString().trim().length() - 1);
      if (impStr.endsWith(typeName)) {
        return imp;
      }
    }
    return null;
  }

  public static Type getTypeNotOnImport(AST ast, Type invExpressionType) {
    try {
      if (invExpressionType.toString().contains("java.lang")) {
        Class.forName(invExpressionType.toString());
      }
    } catch (ClassNotFoundException e) {
      String name = JDTElementUtils.extractSimpleName(invExpressionType);
      if (name.contains("Class1")) {
        invExpressionType = SyntheticClassUtils.getSyntheticType(ast);
      }
      else {
        invExpressionType = TypeUtils.createType(ast, "defaultpkg", name);
      }
    }
    return invExpressionType;
  }
}
