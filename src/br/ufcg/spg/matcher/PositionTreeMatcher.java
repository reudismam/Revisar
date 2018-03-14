package br.ufcg.spg.matcher;

import com.github.gumtreediff.tree.ITree;
import org.eclipse.jdt.core.dom.ASTNode;

public class PositionTreeMatcher implements IMatcher<ITree> {
  private final int start;
  private final int end;
  
  /**
   * constructs a new position matcher.
   * @param start start position
   * @param end end position
   */
  public PositionTreeMatcher(final int start, final int end) {
    this.start = start;
    this.end = end;
  }
  
  /**
   * Constructs a new position matcher.
   * @param target node to be compared.
   */
  public PositionTreeMatcher(final ASTNode target) {
    this.start = target.getStartPosition();
    this.end = target.getStartPosition() + target.getLength();
  }
  
  /**
   * Construct a new position matcher.
   * @param target tree to be compared
   */
  public PositionTreeMatcher(final ITree target) {
    this.start = target.getPos();
    this.end = target.getEndPos();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean evaluate(ITree tree) {
    final int startPosition = tree.getPos();
    final int endPosition = tree.getEndPos();
    return startPosition == start && endPosition == end;
  }
}
