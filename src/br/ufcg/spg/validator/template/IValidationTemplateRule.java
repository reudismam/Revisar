package br.ufcg.spg.validator.template;

import br.ufcg.spg.edit.Edit;

import java.util.List;

/**
 * Rule to analyze.
 */
public interface IValidationTemplateRule {
  
  /**
   * Checks if list of nodes is valid.
   * @param nodes node list.
   * @return true if valid.
   */
  public boolean check(List<Edit> nodes);
}
