package br.ufcg.spg.matcher;

import br.ufcg.spg.tree.RevisarTree;

import com.google.common.base.Objects;

import org.eclipse.jdt.core.dom.ASTNode;

public class ASTNodeMatcher implements IMatcher<RevisarTree<ASTNode>> {
  private final ASTNode value;
  
  /**
   * Constructs a new PositionNodeMatch.
   */
  public ASTNodeMatcher(final ASTNode value) {
    this.value = value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean evaluate(RevisarTree<ASTNode> node) {
    ASTNode nodeValue = node.getValue();
    return Objects.equal(this.value, nodeValue);
  }
}
