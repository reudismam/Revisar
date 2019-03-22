package br.ufcg.spg.refaster;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.transformation.MethodDeclarationUtils;
import br.ufcg.spg.type.TypeUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;

public class ReturnTypeUtils {
  
  private ReturnTypeUtils() {
  }

  /**
   * Configures return type.
   * 
   * @param nodei
   *          node to be analyzed
   * @param refasterRule
   *          Refaster template
   * @param ba
   *          before method and after method
   * @return return type
   */
  public static Tuple<MethodDeclaration, MethodDeclaration> config(final ASTNode nodei, 
      final ASTNode nodej, final CompilationUnit refasterRule, 
      final Tuple<MethodDeclaration, MethodDeclaration> ba) {
    MethodDeclaration before = ba.getItem1();
    MethodDeclaration after = ba.getItem2();
    final int type = nodei.getNodeType();
    if (type == ASTNode.SIMPLE_TYPE || type == ASTNode.PARAMETERIZED_TYPE) {
      return configForType(nodei, nodej, refasterRule, before, after);
    }
    final Type returnType = TypeUtils.extractType(nodei, refasterRule.getAST());
    before = MethodDeclarationUtils.setReturnType(returnType, refasterRule, before);
    after = MethodDeclarationUtils.setReturnType(returnType, refasterRule, after);
    return new Tuple<>(before, after);
  }
  
  /**
   * Configures return type.
   * 
   * @param node
   *          node to be analyzed
   * @param refasterRule
   *          Refaster template
   * @param method
   *          before method and after method
   * @return return type
   */
  public static MethodDeclaration config(final ASTNode node, final CompilationUnit refasterRule, 
      MethodDeclaration method) {
    final int type = node.getNodeType();
    if (type == ASTNode.SIMPLE_TYPE || type == ASTNode.PARAMETERIZED_TYPE) {
      return configForType(node, refasterRule, method);
    }
    final Type returnType = TypeUtils.extractType(node, refasterRule.getAST());
    method = MethodDeclarationUtils.setReturnType(returnType, refasterRule, method);
    return method;
  }

  /**
   * Configures return type for simple type.
   * 
   * @param nodei
   *          node for before version
   * @param nodej
   *          node for after version
   * @param refasterRule
   *          Refaster template
   * @param before
   *          method declaration to match before version
   * @param after
   *          method declaration to match the after version
   * @return configures return type for simple type
   */
  public static Tuple<MethodDeclaration, MethodDeclaration> configForType(
      final ASTNode nodei, final ASTNode nodej,
      final CompilationUnit refasterRule, MethodDeclaration before, MethodDeclaration after) {
    final ASTNode nodeForBefore = TypeUtils.nodeForType(nodei);
    final ASTNode nodeForAfter = TypeUtils.nodeForType(nodej);
    final Type returnTypeForBefore = TypeUtils.extractType(nodeForBefore, refasterRule.getAST());
    final Type returnTypeForAfter = TypeUtils.extractType(nodeForAfter, refasterRule.getAST());
    before = MethodDeclarationUtils.setReturnType(returnTypeForBefore, refasterRule, before);
    after = MethodDeclarationUtils.setReturnType(returnTypeForAfter, refasterRule, after);
    return new Tuple<>(before, after);
  }
  
  /**
  * Configures return type for simple type.
  *
  * @param node
  *          node
  * @param refasterRule
  *          Refaster template
  * @param method
  *          method declaration to match before version
  * @return configures return type for simple type
  */
 public static MethodDeclaration configForType(final ASTNode node,
     final CompilationUnit refasterRule, MethodDeclaration method) {
   final ASTNode nodeForAfter = TypeUtils.nodeForType(node);
   final Type returnTypeForAfter = TypeUtils.extractType(nodeForAfter, refasterRule.getAST());
   method = MethodDeclarationUtils.setReturnType(returnTypeForAfter, refasterRule, method);
   return method;
 }

}
