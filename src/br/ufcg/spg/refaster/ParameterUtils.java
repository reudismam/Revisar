package br.ufcg.spg.refaster;

import java.util.ArrayList;
import java.util.List;

import br.ufcg.spg.stub.StubUtils;
import br.ufcg.spg.transformation.ImportUtils;
import br.ufcg.spg.type.TypeUtils;
import org.eclipse.jdt.core.dom.*;

/**
 * Configure parameters.
 */
public final class ParameterUtils {
  
  private ParameterUtils() {
  }
  
  /**
   * Adds parameter to method.
   * @param types types to be analyzed
   * @param cuUnit compilation unit
   * @param method method 
   * @return method with parameters added.
   */
  @SuppressWarnings("unchecked")
  public static MethodDeclaration addParameter(final List<Type> types, List<ASTNode> varNames,
      final CompilationUnit cuUnit, MethodDeclaration method) {
    final AST ast = cuUnit.getAST();
    final List<ASTNode> parameters = new ArrayList<>();
    for (int i = 0; i < types.size(); i++) {
      // Create a new variable declaration to be added as parameter.
      final SingleVariableDeclaration singleVariableDeclaration = 
          ast.newSingleVariableDeclaration();
      final SimpleName name = ast.newSimpleName(varNames.get(i).toString());
      singleVariableDeclaration.setName(name);
      Type type = types.get(i);
      type = (Type) ASTNode.copySubtree(ast, type);
      singleVariableDeclaration.setType(type);
      singleVariableDeclaration.setVarargs(false);
      final ASTNode singleVariableDeclarationCopy = ASTNode.copySubtree(
          ast, singleVariableDeclaration);
      parameters.add(singleVariableDeclarationCopy);
    }
    method = (MethodDeclaration) ASTNode.copySubtree(ast, method);
    method.parameters().addAll(parameters);
    return method;
  }

  public static MethodDeclaration addParameters(CompilationUnit unit,
                                                List<ASTNode> arguments, CompilationUnit templateClass, MethodDeclaration mDecl) {
    List<Type> argTypes = new ArrayList<>();
    for(ASTNode argNode : arguments) {
      Expression arg = (Expression) argNode;
      Type argType = TypeUtils.extractType(arg, mDecl.getAST());
      //Needed to resolve a bug in eclipse JDT.
      if (argType.toString().contains(".")) {
        argType = ImportUtils.getTypeBasedOnImports(unit, argType.toString().substring(argType.toString().lastIndexOf(".") + 1));
      }
      System.out.println(argNode + " : " + argType);
      argTypes.add(argType);
    }
    mDecl = addParameter(argTypes, arguments, templateClass, mDecl);
    return mDecl;
  }
}
