package br.ufcg.spg.matcher;

import org.eclipse.jdt.core.dom.ASTNode;

public class LargerThanMatcher implements IMatcher<ASTNode> {
  private final int size;
  
  public LargerThanMatcher(int size) {
    this.size = size;
  }

  @Override
  public boolean evaluate(final ASTNode node) {
    return node.getLength() > size;
  }
}
