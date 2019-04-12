package br.ufcg.spg.matcher;

import org.eclipse.jdt.core.dom.ASTNode;

public class AllNodeMatcher implements IMatcher<ASTNode> {
  
  /**
   * Constructs a new PositionNodeMatch.
   */
  public AllNodeMatcher() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean evaluate(ASTNode node) {
    return true;
  }
  
}
