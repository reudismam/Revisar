package br.ufcg.spg.stub;

import br.ufcg.spg.refaster.ParameterUtils;
import br.ufcg.spg.refaster.config.ClassUtils;
import br.ufcg.spg.transformation.JDTElementUtils;
import br.ufcg.spg.transformation.MethodDeclarationUtils;
import br.ufcg.spg.type.TypeUtils;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.util.List;

public class MethodInvocationStub {
  static void stubForMethodInvocation(CompilationUnit unit, CompilationUnit templateClass, Type type, Expression initializer) throws IOException {
    MethodInvocation invocation = (MethodInvocation) initializer;
    TypeDeclaration classDecl = ClassUtils.getTypeDeclaration(templateClass);
    Type classType = TypeUtils.extractType(invocation.getExpression(), classDecl.getAST());
    boolean isStatic = classType.toString().equals("void");
    System.out.println("Before\n");
    System.out.println(templateClass.toString());
    MethodDeclaration declaration = templateClass.getAST().newMethodDeclaration();
    MethodDeclaration mDecl = MethodDeclarationUtils.setReturnType(type, templateClass, declaration);
    MethodDeclarationUtils.addBody(templateClass, mDecl);
    MethodDeclarationUtils.addThrowStatement(mDecl);
    MethodDeclarationUtils.setName(mDecl, invocation.getName());
    MethodDeclarationUtils.addModifier(mDecl, Modifier.ModifierKeyword.PUBLIC_KEYWORD);
    if (isStatic) {
      MethodDeclarationUtils.addModifier(mDecl, Modifier.ModifierKeyword.STATIC_KEYWORD);
    }
    List<ASTNode> arguments = (List<ASTNode>) invocation.arguments();
    ParameterUtils.addParameters(unit, initializer, arguments, templateClass, classDecl, mDecl);
    System.out.println("After\n");
    classDecl.bodyDeclarations().add(mDecl);
    System.out.println(templateClass.toString());
    JDTElementUtils.saveClass(templateClass, classDecl);
  }
}
