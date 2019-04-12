package br.ufcg.spg.replacement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Replacement<T> {
  private String unification;
  private T node;
  
  /**
   * Constructs a new instance.
   * @param unification unification
   */
  public Replacement(final String unification, final T node) {
    super();
    this.unification = unification;
    this.node = node;
  }

  public String getUnification() {
    return unification;
  }
  
  public void setUnification(final String unification) {
    this.unification = unification;
  }
  
  public T getNode() {
    return node;
  }
  
  public void setNode(final T node) {
    this.node = node;
  }
  
  /**
   * Verifies whether edited nodes are result from an unification.
   */
  public boolean isUnification() {
    final Pattern pattern = Pattern.compile(ReplacementUtils.REGEX);
    final Matcher matcher = pattern.matcher(unification);
    return matcher.find();
  } 
  
  @Override
  public int hashCode() {
    return super.hashCode();
  }
  
  @Override
  public String toString() {
    return "unification: " + unification + "\n"
        + "before: " + node + "\n";
  }
}
