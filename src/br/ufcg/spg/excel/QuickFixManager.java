package br.ufcg.spg.excel;

import java.util.ArrayList;
import java.util.List;

public class QuickFixManager {
  private static QuickFixManager instance;
  
  private List<QuickFix> quickFixes;
  
  private List<QuickFix> badPatterns;
  
  private List<QuickFix> potentialPatterns;
  
  private QuickFixManager() {
    quickFixes = new ArrayList<>();
    badPatterns = new ArrayList<>();
    potentialPatterns = new ArrayList<>();
  }
  
  /**
   * gets singleton instance.
   */
  public static QuickFixManager getInstance() {
    if (instance == null) {
      instance = new QuickFixManager();
    }
    return instance;
  }

  public List<QuickFix> getQuickFixes() {
    return quickFixes;
  }

  public void setQuickFixes(List<QuickFix> quickFixes) {
    this.quickFixes = quickFixes;
  }

  public List<QuickFix> getBadPatterns() {
    return badPatterns;
  }

  public void setBadPatterns(List<QuickFix> badPatterns) {
    this.badPatterns = badPatterns;
  }

  public List<QuickFix> getPotentialPatterns() {
    return potentialPatterns;
  }

  public void setPotentialPatterns(List<QuickFix> potentialPatterns) {
    this.potentialPatterns = potentialPatterns;
  }
}
