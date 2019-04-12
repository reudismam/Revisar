package br.ufcg.spg.constraint;

import org.eclipse.jdt.core.dom.ASTNode;

public interface IConstraintRule {
  /**
   * Determines whether the AST node is valid.
   * @param node - analyzed node
   * @return true is the AST node is valid.
   */
  public boolean isValid(ASTNode node);
  
  /**
   * Determines if this rule is applicable
   * to the provided AST node.
   * @param node node to be analyzed.
   * @return true if it is applicable.
   */
  public boolean isApplicableTo(ASTNode node);
}
