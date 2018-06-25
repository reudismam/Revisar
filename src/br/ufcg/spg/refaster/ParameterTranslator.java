package br.ufcg.spg.refaster;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.replacement.Replacement;
import br.ufcg.spg.type.TypeUtils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

public class ParameterTranslator {
  /**
   * Configure parameter.
   * @param targetList target list
   * @param refasterRule Refaster rule current
   * @param ba before and after node
   * @return new before and after version with parameters modified.
   */
  public static Tuple<MethodDeclaration, MethodDeclaration> config(
      final List<Replacement<ASTNode>> targetList,
      final CompilationUnit refasterRule, 
      final Tuple<MethodDeclaration, MethodDeclaration> ba) {
    MethodDeclaration before = ba.getItem1();
    MethodDeclaration after = ba.getItem2();
    final List<Type> paramTypes = new ArrayList<>();
    for (int i = 0; i < targetList.size(); i++) {
      final Replacement<ASTNode> replacement = targetList.get(i);
      if (!replacement.isUnification()) {
        continue;
      }
      final ASTNode tbefore = targetList.get(i).getNode();
      final Type paramType = TypeUtils.extractType(tbefore, refasterRule.getAST());
      paramTypes.add(paramType);
    }
    before = addParameter(paramTypes, refasterRule, before);
    after = addParameter(paramTypes, refasterRule, after);
    return new Tuple<>(before, after);
  }

  /**
   * Adds parameter to method
   * @param types types to be analyzed
   * @param cuUnit compilation unit
   * @param method method 
   * @return method with parameters added.
   */
  @SuppressWarnings("unchecked")
  private static MethodDeclaration addParameter(
      final List<Type> types, final CompilationUnit cuUnit, MethodDeclaration method) {
    final AST ast = cuUnit.getAST();
    final List<ASTNode> parameters = new ArrayList<>();
    for (int i = 0; i < types.size(); i++) {
      // Create a new variable declaration to be added as parameter.
      final SingleVariableDeclaration singleVariableDeclaration = 
          ast.newSingleVariableDeclaration();
      final SimpleName name = ast.newSimpleName("v_" + i);
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

}
