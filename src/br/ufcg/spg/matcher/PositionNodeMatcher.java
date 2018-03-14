package br.ufcg.spg.matcher;

import com.github.gumtreediff.tree.ITree;
import org.eclipse.jdt.core.dom.ASTNode;

public class PositionNodeMatcher implements IMatcher<ASTNode> {
  private final int start;
  private final int end;
  
  /**
   * Constructs a new PositionNodeMatch.
   * @param start start position
   * @param end end position
   */
  public PositionNodeMatcher(final int start, final int end) {
    this.start = start;
    this.end = end;
  }
  
  /**
   * Constructs a new PositionNodeMatch.
   * @param target target node
   */
  public PositionNodeMatcher(final ASTNode target) {
    this.start = target.getStartPosition();
    this.end = target.getStartPosition() + target.getLength();
  }
  
  /**
   * Constructs a new PositionNodeMatch instance.
   * @param target target node
   */
  public PositionNodeMatcher(final ITree target) {
    this.start = target.getPos();
    this.end = target.getEndPos();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean evaluate(ASTNode node) {
    final int startPosition = node.getStartPosition();
    final int length = node.getLength();
    return startPosition == start && startPosition + length == end;
  }
  
}
