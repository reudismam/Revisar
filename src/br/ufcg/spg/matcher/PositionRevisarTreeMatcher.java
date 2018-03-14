package br.ufcg.spg.matcher;

import br.ufcg.spg.tree.RevisarTree;
import com.github.gumtreediff.tree.ITree;

import org.eclipse.jdt.core.dom.ASTNode;

public class PositionRevisarTreeMatcher<T> implements IMatcher<RevisarTree<T>> {
  private final int start;
  private final int end;
  
  /**
   * constructs a new position matcher.
   * @param start start position
   * @param end end position
   */
  public PositionRevisarTreeMatcher(final int start, final int end) {
    this.start = start;
    this.end = end;
  }
  
  /**
   * Constructs a new position matcher.
   * @param target node to be compared.
   */
  public PositionRevisarTreeMatcher(final ASTNode target) {
    this.start = target.getStartPosition();
    this.end = target.getStartPosition() + target.getLength();
  }
  
  /**
   * Construct a new position matcher.
   * @param target tree to be compared
   */
  public PositionRevisarTreeMatcher(final ITree target) {
    this.start = target.getPos();
    this.end = target.getEndPos();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean evaluate(RevisarTree<T> rtree) {
    if (rtree.getValue() instanceof ASTNode) {
      final ASTNode node = (ASTNode)rtree.getValue();
      final int startPosition = node.getStartPosition();
      final int length = node.getLength();
      return startPosition == start && startPosition + length == end;
    }
    final ITree tree = (ITree) rtree.getValue();
    final int startPosition = tree.getPos();
    final int endPosition = tree.getEndPos();
    return startPosition == start && endPosition == end;
  }
}
