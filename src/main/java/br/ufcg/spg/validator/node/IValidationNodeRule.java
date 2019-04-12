package br.ufcg.spg.validator.node;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Rule to analyze.
 */
public interface IValidationNodeRule {
  
  /**
   * Checks if list of nodes is valid.
   * @param nodes node list.
   * @return true if valid.
   */
  public boolean checker(List<ASTNode> nodes);
  
  public boolean checkerTemplate(List<String> nodes);

}
