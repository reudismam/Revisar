package br.ufcg.spg.refaster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.stub.StubUtils;
import br.ufcg.spg.transformation.ClassRepository;
import br.ufcg.spg.transformation.ImportUtils;
import br.ufcg.spg.transformation.JDTElementUtils;
import br.ufcg.spg.transformation.SyntheticClassUtils;
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
  public static MethodDeclaration addParameter(final List<Type> types, List<String> varNames,
      final CompilationUnit cuUnit, MethodDeclaration method) {
    final AST ast = cuUnit.getAST();
    final List<ASTNode> parameters = new ArrayList<>();
    for (int i = 0; i < types.size(); i++) {
      // Create a new variable declaration to be added as parameter.
      final SingleVariableDeclaration singleVariableDeclaration = 
          ast.newSingleVariableDeclaration();
      final SimpleName name = ast.newSimpleName(varNames.get(i));
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
    List<String> varNames = new ArrayList<>();
    int i = 0;
    for(ASTNode argNode : arguments) {
      varNames.add("v_" + i++);
      Expression arg = (Expression) argNode;
      Type argType;
      if (arg.toString().equals("null")) {
        argType = TypeUtils.createType(unit, "java.lang", "Object");
      } else {
        argType = TypeUtils.extractType(arg, arg.getAST());
      }
      if (arg instanceof MethodInvocation) {
        try {
        if (argType.toString().equals("void")) {
          argType = SyntheticClassUtils.getSyntheticType(unit.getAST());
          CompilationUnit synthetic = SyntheticClassUtils.createSyntheticClass(unit);
          JDTElementUtils.saveClass(synthetic);
        }
          StubUtils.processMethodInvocation(unit, argType, arg);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      else if (arg instanceof ClassInstanceCreation) {
        ClassInstanceCreation initializer = (ClassInstanceCreation) arg;
        Type type = TypeUtils.extractType(arg, arg.getAST());
        try {
          StubUtils.processClassCreation(unit, type, initializer);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      //Needed to resolve a bug in eclipse JDT.
      if (argType.toString().contains(".") && !argType.toString().contains("syntethic")) {
        System.out.println(argType);
        String typeName = JDTElementUtils.extractSimpleName(argType);
        System.out.println(typeName);
        argType = ImportUtils.getTypeBasedOnImports(unit, typeName);
      }
      System.out.println(argNode + " : " + argType);
      argTypes.add(argType);
    }
    mDecl = addParameter(argTypes, varNames, templateClass, mDecl);
    return mDecl;
  }
}
