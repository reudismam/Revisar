package br.ufcg.spg.constraint.rule;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class RuleUtils {
  /**
   * Gets type declaration.
   */
  public static TypeDeclaration getTypeDeclaration(final ASTNode node) {
    if (node instanceof TypeDeclaration) {
      return (TypeDeclaration) node;
    }
    return getTypeDeclaration(node.getParent());
  }
}
