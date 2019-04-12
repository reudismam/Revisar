package br.ufcg.spg.filter;

/**
 * Variable declaration filter.
 */
public class PatternFilter {

  private String beforePattern;
  private String afterPattern;
  
  /**
   * Constructor.
   */
  public PatternFilter() {
  }
  
  /**
   * Constructor.
   */
  public PatternFilter(String beforePattern, String afterPattern) {
    this.beforePattern = beforePattern;
    this.afterPattern = afterPattern;
  }

  /**
   * Verify if this filter match the before and after patterns.
   */
  public boolean match(String srcOutput, String dstOutput) {
    if (srcOutput.matches(beforePattern) && dstOutput.matches(afterPattern)) {
      return true;
    }
    return false;
  }
}
