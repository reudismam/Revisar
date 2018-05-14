package br.ufcg.spg.search.evaluator;

import com.google.common.base.Objects;
import org.eclipse.jdt.core.dom.ASTNode;

public class ASTNodeEvaluator implements IEvaluator {
  
  private final ASTNode value;
  
  public ASTNodeEvaluator(final ASTNode value) {
    this.value = value;
  }

  @Override
  public boolean evaluate(final ASTNode node) {
    return Objects.equal(this.value, node);
  }
}
