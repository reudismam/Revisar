package br.ufcg.spg.config;

import br.ufcg.spg.edit.Edit;

/**
 * Technique configuration.
 */
public final class TechniqueConfig {

  /**
   * Singleton instance.
   */
  private static TechniqueConfig instance;

  /**
   * Specifies if all commits must be analyzed.
   */
  private boolean allCommits = false;

  /**
   * Number of commits to analyze if allCommits is enabled.
   */
  private int editsToAnalyze = 100;

  /**
   * True if type is include in the template.
   */
  private boolean templateIncludesType = false;

  /**
   * Specifies d-cap to analyze.
   */
  private int dcap = 1;

  /**
   * Max number of edits to extract from database.
   */
  private int maxEditsToReturn = 10000;
  
  /**
   * Defines whether Refaster transformation should be generated.
   */
  private boolean createRule = false;
  
  private boolean fullTemplateRules = false;
  

  private TechniqueConfig() {
  }

  /**
   * Gets singleton instance.
   * 
   * @return singleton instance
   */
  public static synchronized TechniqueConfig getInstance() {
    if (instance == null) {
      instance = new TechniqueConfig();
    }
    return instance;
  }

  public boolean isAllCommits() {
    return allCommits;
  }

  public void setAllCommits(final boolean allCommits) {
    this.allCommits = allCommits;
  }

  public int getEditsToAnalyze() {
    return editsToAnalyze;
  }

  public void setEditsToAnalyze(final int editsToAnalyze) {
    this.editsToAnalyze = editsToAnalyze;
  }

  public boolean isTemplateIncludesType() {
    return templateIncludesType;
  }

  public void setTemplateIncludesType(final boolean templateIncludesType) {
    this.templateIncludesType = templateIncludesType;
  }

  public int getDcap() {
    return dcap;
  }
  
  /**
   * Get d-cap for an edit.
   * @param edit edit to be analyzed.
   * @return d-cap most appropriated.
   */
  public String getDcap(final Edit edit) {
    if (dcap == 1) {
      return edit.getDcap1();
    }
    if (dcap == 2) {
      return edit.getDcap2();
    }
    return edit.getDcap3();
  }
  
  public void setDcap(final int dcap) {
    this.dcap = dcap;
  }

  public int getMaxEditsToReturn() {
    return maxEditsToReturn;
  }

  public void setMaxEditsToReturn(final int maxEditsToReturn) {
    this.maxEditsToReturn = maxEditsToReturn;
  }

  public boolean isCreateRule() {
    return createRule;
  }

  public void setCreateRule(final boolean createRule) {
    this.createRule = createRule;
  }

  public boolean isFullTemplateRules() {
    return fullTemplateRules;
  }

  public void setFullTemplateRules(boolean fullTemplateRules) {
    this.fullTemplateRules = fullTemplateRules;
  }
}
