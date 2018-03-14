package br.ufcg.spg.matcher;

import com.github.gumtreediff.tree.ITree;
import org.eclipse.jdt.core.dom.ASTNode;

public class PositionMatchCalculator extends AbstractMatchCalculator {
  private final int start;
  private final int end;
  
  public PositionMatchCalculator(final int start, final int end) {
    this.start = start;
    this.end = end;
  }
  
  public PositionMatchCalculator(final ASTNode target) {
    this.start = target.getStartPosition();
    this.end = target.getStartPosition() + target.getLength();
  }
  
  public PositionMatchCalculator(final ITree target) {
    this.start = target.getPos();
    this.end = target.getEndPos();
  }
  
  @Override
  protected boolean evaluate(final ITree st) {
    final int startPosition = st.getPos();
    final int endPosition = st.getEndPos();
    return startPosition == start && endPosition == end;
  }

  @Override
  protected boolean evaluate(final ASTNode st) {
    final int startPosition = st.getStartPosition();
    final int length = st.getLength();
    return startPosition == start && startPosition + length == end;
  }
  
}
