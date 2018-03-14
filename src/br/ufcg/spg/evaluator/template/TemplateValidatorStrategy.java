package br.ufcg.spg.evaluator.template;

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
      ITemplateChecker ch = RuleTemplateChecker.create(0, srcEdits);
      if (!ch.check()) {
        return false;
      }
      ch = new MatchTemplateChecker(srcAu, dstAu, srcEdits);
      final boolean ba = ch.check();
      if (!ba) {
        return ba;
      }
      return true;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
