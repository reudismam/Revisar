package br.ufcg.spg.search.evaluator;

import com.google.common.base.Objects;
import org.eclipse.jdt.core.dom.ASTNode;

public class ValueEvaluator implements IEvaluator {
  
  private final String value;
  
  public ValueEvaluator(final String value) {
    this.value = value;
  }

  @Override
  public boolean evaluate(final ASTNode node) {
    final String nodeValue = node.toString();
    return Objects.equal(this.value, nodeValue);
  }
}
