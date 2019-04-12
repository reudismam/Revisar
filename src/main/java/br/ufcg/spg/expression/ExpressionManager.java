package br.ufcg.spg.expression;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class ExpressionManager {
  
  /**
   * Gets qualified name of an ASTNode.
   * 
   * @param node - node to extract the expression.
   * @return expression of an ASTNode
   */
  public static String qualifiedName(final ASTNode node) {
    if (node instanceof Expression) {
      final Expression expsource = (Expression) node;
      final ITypeBinding typeBindingSource = expsource.resolveTypeBinding();
      if (typeBindingSource != null) {
        return typeBindingSource.getQualifiedName();
      }
    }
    return null;
  }
  
  /**
   * Gets expression of an ASTNode.
   * 
   * @param node - node to extract the expression.
   * @return expression of an ASTNode
   */
  public static Expression expression(final ASTNode node) {
    if (node instanceof Expression) {
      final Expression expsource = (Expression) node;
      return expsource;
    }
    return null;
  }
}
