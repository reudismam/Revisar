package br.ufcg.spg.excel;

import java.util.ArrayList;
import java.util.List;

public class QuickFixManager {
  private static QuickFixManager instance;
  
  private List<QuickFix> quickFixes;
  
  private QuickFixManager() {
    quickFixes = new ArrayList<>();
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
}
