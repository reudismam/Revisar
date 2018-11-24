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
  
  private ParameterTranslator() {
  }
  
//  /**
//   * Configure parameter.
//   * @param targetList target list
//   * @param refasterRule Refaster rule current
//   * @param ba before and after node
//   * @return new before and after version with parameters modified.
//   */
//  public static Tuple<MethodDeclaration, MethodDeclaration> config(
//      List<Replacement<ASTNode>> targetList,
//      final CompilationUnit refasterRule, 
//      final Tuple<MethodDeclaration, MethodDeclaration> ba) {
//    List<ASTNode> nodes = getNodes(targetList);
//    List<Integer> filtered = ReturnStmTranslator.filterTypes(nodes);
//    List<Replacement<ASTNode>> newTargetList = new ArrayList<>();
//    for (int i = 0; i < targetList.size(); i++) {
//      if (!filtered.contains(i)) {
//        newTargetList.add(targetList.get(i));
//      }
//    }
//    targetList = newTargetList;
//    final List<Type> paramTypes = extractTypes(getNodes(targetList), refasterRule.getAST());
//    MethodDeclaration before = ba.getItem1();
//    MethodDeclaration after = ba.getItem2();
//    before = addParameter(paramTypes, refasterRule, before);
//    after = addParameter(paramTypes, refasterRule, after);
//    return new Tuple<>(before, after);
//  }

//  private static List<ASTNode> getNodes(List<Replacement<ASTNode>> targetList) {
//    List<ASTNode> nodes = new ArrayList<>();
//    for (Replacement<ASTNode> node : targetList) {
//      nodes.add(node.getNode());
//    }
//    return nodes;
//  }

  public static List<Type> extractTypes(List<ASTNode> targetList, 
      final AST refasterRule) {
    final List<Type> paramTypes = new ArrayList<>();
    for (int i = 0; i < targetList.size(); i++) {
      final ASTNode tbefore = targetList.get(i);
      final Type paramType = TypeUtils.extractType(tbefore, refasterRule);
      paramTypes.add(paramType);
    }
    return paramTypes;
  }

  /**
   * Adds parameter to method.
   * @param types types to be analyzed
   * @param cuUnit compilation unit
   * @param method method 
   * @return method with parameters added.
   */
  @SuppressWarnings("unchecked")
  public static MethodDeclaration addParameter(final List<Type> types, List<ASTNode> holes, 
      final CompilationUnit cuUnit, MethodDeclaration method) {
    final AST ast = cuUnit.getAST();
    final List<ASTNode> parameters = new ArrayList<>();
    for (int i = 0; i < types.size(); i++) {
      // Create a new variable declaration to be added as parameter.
      final SingleVariableDeclaration singleVariableDeclaration = 
          ast.newSingleVariableDeclaration();
      final SimpleName name = ast.newSimpleName(holes.get(i).toString());
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
