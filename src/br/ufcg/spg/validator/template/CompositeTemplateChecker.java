package br.ufcg.spg.validator.template;

import br.ufcg.spg.edit.Edit;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks rules.
 */
public final class CompositeTemplateChecker implements ITemplateChecker {
  
  /**
   * List of rule each node must be checked in each node.
   */
  private final transient List<ITemplateChecker> rules;

  /**
   * Constructor
   * 
   * @param depth
   *          depth on the AST tree.
   * @param rules
   *          rules to be analyzed.
   */
  private CompositeTemplateChecker(final List<ITemplateChecker> rules) {
    this.rules = rules;
  }

  /**
   * Creates a instance of a rule checker.
   * @param srcEdits
   *          source code edits
   * @return a new instance of a rule checker.
   */
  public static CompositeTemplateChecker create(final String srcAu, 
      final String dstAu, final List<Edit> srcEdits) {
    final List<ITemplateChecker> rules = new ArrayList<>();
    final MethodInvocationTemplateChecker minvo = new MethodInvocationTemplateChecker(srcEdits);
    final SimpleTypeTemplateChecker stype = new SimpleTypeTemplateChecker(srcAu, srcEdits);
    rules.add(minvo);
    rules.add(stype);
    return new CompositeTemplateChecker(rules);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValidUnification() {
    try {
      for (final ITemplateChecker rule : rules) {
        if (!rule.isValidUnification()) {
          return false;
        }
      }
      return true;
    } catch (final Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
