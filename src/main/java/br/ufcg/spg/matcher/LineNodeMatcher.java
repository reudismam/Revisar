package br.ufcg.spg.matcher;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import br.ufcg.spg.source.SourceUtils;

public class LineNodeMatcher implements IMatcher<ASTNode> {
  private final int line;
  private CompilationUnit unit;
  
  /**
   * Constructs a new LineNodeMatcher.
   */
  public LineNodeMatcher(final CompilationUnit unit, final int line) {
    this.unit = unit;
    this.line = line;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean evaluate(ASTNode node) {
    final int start = node.getStartPosition();
    final int end = node.getStartPosition() + node.getLength();
    final boolean isSingle = SourceUtils.isSingleLine(unit, start, end);
    final int nodeLine = SourceUtils.getLineNumber(unit, start);
    return line == nodeLine && isSingle;
  } 
}
