package br.ufcg.spg.validator.template;

import br.ufcg.spg.edit.Edit;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks rules.
 */
public final class RuleTemplateChecker implements ITemplateChecker {
  
  /**
   * List of rule each node must be checked in each node.
   */
  private final transient List<IValidationTemplateRule> rules;

  /**
   * Edits to analyze.
   */
  private final transient List<Edit> srcEdits;

  /**
   * Constructor
   * 
   * @param depth
   *          depth on the AST tree.
   * @param rules
   *          rules to be analyzed.
   */
  private RuleTemplateChecker(final List<IValidationTemplateRule> rules, 
      final List<Edit> srcEdits) {
    super();
    this.rules = rules;
    this.srcEdits = srcEdits;
  }

  /**
   * Creates a instance of a rule checker.
   * 
   * @param depth
   *          depth on the three to be analyzed.
   * @param srcEdits
   *          source code edits
   * @return a new instance of a rule checker.
   */
  public static RuleTemplateChecker create(final int depth, final List<Edit> srcEdits) {
    final List<IValidationTemplateRule> rules = new ArrayList<IValidationTemplateRule>();
    final MethodInvocationTemplateChecker minvo = new MethodInvocationTemplateChecker();
    rules.add(minvo);
    return new RuleTemplateChecker(rules, srcEdits);
  }

  @Override
  public boolean check() {
    try {
      //final List<String> templates = new ArrayList<>();
      //for (final Edit srcEdit : srcEdits) {
      //  templates.add(srcEdit.getTemplate());
      //}
      for (final IValidationTemplateRule rule : rules) {
        if (!rule.check(srcEdits)) {
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
