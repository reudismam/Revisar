package br.ufcg.spg.refaster.config;

import br.ufcg.spg.stub.StubUtils;
import br.ufcg.spg.transformation.JDTElementUtils;
import org.eclipse.jdt.core.dom.*;

public class ClassUtils {
  public static TypeDeclaration createClassDeclaration(CompilationUnit unit, String baseName, ASTNode imp) {
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(unit);
    AST ast = classDecl.getAST();
    Type packageType = StubUtils.getTypeFromImport(baseName, ast, imp);
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
}
