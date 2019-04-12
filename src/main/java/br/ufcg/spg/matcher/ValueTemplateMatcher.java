package br.ufcg.spg.matcher;

import br.ufcg.spg.tree.RevisarTree;
import com.google.common.base.Objects;

public class ValueTemplateMatcher implements IMatcher<RevisarTree<String>> {
  private final String value;
  
  /**
   * Constructs a new PositionNodeMatch.
   */
  public ValueTemplateMatcher(final String value) {
    this.value = value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean evaluate(RevisarTree<String> node) {
    final String nodeValue = node.getValue();
    return Objects.equal(this.value, nodeValue);
  }
}
