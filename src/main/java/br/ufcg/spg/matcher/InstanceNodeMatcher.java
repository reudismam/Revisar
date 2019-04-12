package br.ufcg.spg.matcher;

import org.eclipse.jdt.core.dom.ASTNode;

public class InstanceNodeMatcher implements IMatcher<ASTNode> {
  private final Class clazz;

  /**
   * Constructs a new PositionNodeMatch.
   */
  public InstanceNodeMatcher(final Class clazz) {
    this.clazz = clazz;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean evaluate(ASTNode node) {
    final Class classType = node.getClass();
    try {
      clazz.cast(node);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
