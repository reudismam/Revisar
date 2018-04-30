package br.ufcg.spg.validator.template;

import br.ufcg.spg.edit.Edit;

import java.util.List;

/**
 * EValidation based on template.
 */
public class TemplateValidatorStrategy implements ITransformationValidatorStrategy {

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValidTrans(final List<Edit> srcEdits, final String srcAu, final String dstAu) {
    try {
      ITemplateChecker ch = RuleTemplateChecker.create(srcAu, dstAu, srcEdits);
      if (!ch.checkIsValidUnification()) {
        return false;
      }
      ch = new MatchTemplateChecker(srcAu, dstAu, srcEdits);
      return ch.checkIsValidUnification();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
