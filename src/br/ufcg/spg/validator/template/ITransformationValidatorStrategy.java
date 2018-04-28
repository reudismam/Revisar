package br.ufcg.spg.validator.template;

import br.ufcg.spg.edit.Edit;
import java.util.List;

/**
 * Class that evaluate cluster.
 *
 */
public interface ITransformationValidatorStrategy {
  
  /**
   * Evaluate cluster.
   * @param csrcEdits cluster edit
   * @param srcAu src anti-unification
   * @param dstAu dst anti-unification
   * @return true if edit forms a valid cluster
   */
  public boolean isValidTrans(List<Edit> csrcEdits, String srcAu, String dstAu);
}
