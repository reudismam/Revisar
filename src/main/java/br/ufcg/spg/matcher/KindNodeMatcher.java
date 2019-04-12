package br.ufcg.spg.matcher;

import org.eclipse.jdt.core.dom.ASTNode;

public class KindNodeMatcher implements IMatcher<ASTNode> {
  private final int kind;
  
  /**
   * Constructs a new PositionNodeMatch.
   */
  public KindNodeMatcher(final int kind) {
    this.kind = kind;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean evaluate(ASTNode node) {
    final int nodekind = node.getNodeType();
    return nodekind == kind;
  }
  
}
