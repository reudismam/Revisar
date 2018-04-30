package br.ufcg.spg.validator.template;

import br.ufcg.spg.analyzer.util.AnalyzerUtil;
import br.ufcg.spg.antiunification.AntiUnificationUtils;
import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.cluster.UnifierCluster;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.ValueTemplateMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.RevisarTreeMatchCalculator;
import br.ufcg.spg.tree.RevisarTree;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Checks mapping.
 */
public class SimpleTypeTemplateChecker implements ITemplateValidator {
  /**
   * Source code anti-unification.
   */
  private final transient String srcAu;
  /**
   * Edit list.
   */
  private final transient List<Edit> srcEdits;
  
  /**
   * Creates a new instance.
   */
  public SimpleTypeTemplateChecker(final String srcAu, final List<Edit> srcEdits) {
    this.srcAu = srcAu;
    this.srcEdits = srcEdits;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValidUnification() {
    return !isTemplateAbstractSimpleType();
  }

  /**
   * Verifies whether template abstract any simple type.
   */
  private boolean isTemplateAbstractSimpleType() {
    final Edit firstEdit = srcEdits.get(0);
    final Map<String, String> substutings = AntiUnificationUtils.getUnifierMatching(
        firstEdit.getTemplate(), srcAu);
    final AntiUnifier unifier = UnifierCluster.computeUnification(firstEdit.getTemplate(), srcAu);
    final RevisarTree<String> tree = unifier.toRevisarTree();
    for (final Entry<String, String> match : substutings.entrySet()) {
      String valueKey = match.getKey();
      if (!valueKey.contains("hash")) {
        continue;
      }
      valueKey = "#" + (valueKey.substring(5, valueKey.length()));
      final IMatcher<RevisarTree<String>> matcher = new ValueTemplateMatcher(valueKey);
      final MatchCalculator<RevisarTree<String>> calc = new RevisarTreeMatchCalculator<>(matcher);
      final RevisarTree<String> value = calc.getNode(tree);
      final String simpleTypeLabel = AnalyzerUtil.getLabel(ASTNode.SIMPLE_TYPE);
      final boolean parentContains = ancestorsContainLabel(value, simpleTypeLabel);
      if (parentContains) {
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
