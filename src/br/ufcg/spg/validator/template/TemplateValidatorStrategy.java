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
      ITemplateValidator ch = CompositeTemplateValidator.create(srcAu, dstAu, srcEdits);
      if (!ch.isValidUnification()) {
        return false;
      }
      ch = new MappingTemplateValidator(srcAu, dstAu, srcEdits);
      return ch.isValidUnification();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
