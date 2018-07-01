package br.ufcg.spg.validator.template;

import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.antiunification.AntiUnifierUtils;
import br.ufcg.spg.antiunification.algorithm.URAUC;
import br.ufcg.spg.config.TechniqueConfig;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.ValueTemplateMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.RevisarTreeMatchCalculator;
import br.ufcg.spg.tree.RevisarTree;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Checks mapping.
 */
public class LabelTemplateValidator implements ITemplateValidator {
  /**
   * Source code anti-unification.
   */
  private final transient String srcAu;
  /**
   * Edit list.
   */
  private final transient List<Edit> srcEdits;
  /**
   * Label to be evaluated.
   */
  private final transient String label;
  
  /**
   * Creates a new instance.
   */
  public LabelTemplateValidator(final String srcAu, final List<Edit> srcEdits, 
      final String typeLabel) {
    this.srcAu = srcAu;
    this.srcEdits = srcEdits;
    this.label = typeLabel;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValidUnification() {
    return !isHoleLabel();
  }

  /**
   * Verifies whether template abstract any node with given label.
   * @throws IOException 
   * @throws ControlledException 
   * @throws JustificationException 
   */
  private boolean isHoleLabel() {
    final Edit firstEdit = srcEdits.get(0);
    final Map<String, String> substutings = AntiUnifierUtils.getUnifierMatching(
        srcAu, firstEdit.getPlainTemplate());
    final AntiUnifier unifier = AntiUnifierUtils.antiUnify(srcAu, firstEdit.getPlainTemplate());
    final RevisarTree<String> tree = unifier.toRevisarTree();
    for (final Entry<String, String> match : substutings.entrySet()) {
      String valueKey = match.getKey();
      if (!valueKey.contains("hash")) {
        continue;
      }
      valueKey = "#" + valueKey.substring(5);
      final IMatcher<RevisarTree<String>> matcher = new ValueTemplateMatcher(valueKey);
      final MatchCalculator<RevisarTree<String>> calc = new RevisarTreeMatchCalculator<>(matcher);
      final RevisarTree<String> value = calc.getNode(tree);
      final boolean ancestorsContains = ancestorsContainLabel(value, label);
      if (ancestorsContains) {
        return true;
      }
    }
    return false;
  }

  /**
   * Verifies whether any of the ancestor is of a specific label.
   */
  private boolean ancestorsContainLabel(final RevisarTree<String> value, String label) {
    RevisarTree<String> parent = value;
    while (parent != null) {
      if (parent.getValue().equals(label)) {
        return true;
      }
      parent = parent.getParent();
    }
    return false;
  }
}
