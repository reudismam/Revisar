package br.ufcg.spg.matcher;

import com.google.common.base.Objects;
import org.eclipse.jdt.core.dom.ASTNode;

public class ValueNodeMatcher implements IMatcher<ASTNode> {
  private final String value;
  
  /**
   * Constructs a new PositionNodeMatch.
   */
  public ValueNodeMatcher(final String value) {
    this.value = value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean evaluate(ASTNode node) {
    final String nodeValue = node.toString();
    return Objects.equal(this.value, nodeValue);
  }
  
}
