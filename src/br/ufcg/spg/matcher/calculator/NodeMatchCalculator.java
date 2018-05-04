package br.ufcg.spg.matcher.calculator;

import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.node.util.ASTNodeUtils;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Performs match operations.
 *
 */
public class NodeMatchCalculator extends MatchCalculator<ASTNode> {
  
  /**
   * Constructor.
   */
  public NodeMatchCalculator(IMatcher<ASTNode> evaluator) {
    super(evaluator);
  }
  
  /**
   * {@inheritDoc}
   */
  public List<ASTNode> chilren(ASTNode st) {
    final List<Object> childrenObjects = ASTNodeUtils.getChildren(st);
    final List<ASTNode> children = ASTNodeUtils.normalize(childrenObjects);
    return children;
  }
}
