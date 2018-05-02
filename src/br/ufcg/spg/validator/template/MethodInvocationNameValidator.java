package br.ufcg.spg.validator.template;

import br.ufcg.spg.analyzer.util.AnalyzerUtil;
import br.ufcg.spg.antiunification.AntiUnifierUtils;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.ValueTemplateMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.RevisarTreeMatchCalculator;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeParser;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Checks mapping.
 */
public class MethodInvocationNameValidator implements ITemplateValidator {
  /**
   * Edit list.
   */
  private final transient List<Edit> srcEdits;
  
  /**
   * Creates a new instance.
   */
  public MethodInvocationNameValidator(final List<Edit> srcEdits) {
    this.srcEdits = srcEdits;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValidUnification() {
    return !isHoleLabel();
  }

  /**
   * Verifies whether template abstract any simple type.
   */
  private boolean isHoleLabel() {
    final Edit firstEdit = srcEdits.get(0);
    final Edit lastEdit = srcEdits.get(srcEdits.size() - 1);
    final Map<String, String> substutings = AntiUnifierUtils.getUnifierMatching(
        firstEdit.getTemplate(), lastEdit.getTemplate());
    final RevisarTree<String> tree = RevisarTreeParser.parser(firstEdit.getTemplate());
    for (final Entry<String, String> match : substutings.entrySet()) {
      String valueKey = match.getKey();
      if (!valueKey.contains("name_")) {
        continue;
      }
      final IMatcher<RevisarTree<String>> matcher = new ValueTemplateMatcher(valueKey);
      final MatchCalculator<RevisarTree<String>> calc = new RevisarTreeMatchCalculator<>(matcher);
      final RevisarTree<String> value = calc.getNode(tree);
      final String label = AnalyzerUtil.getLabel(ASTNode.METHOD_INVOCATION);
      final boolean isRename = isRename(value, label);
      if (isRename) {
        return true;
      }
    }
    return false;
  }

  /**
   * Verifies whether any of the ancestor is of a specific label.
   */
  private boolean isRename(final RevisarTree<String> value, String label) {
    if (value == null) {
      throw new RuntimeException("node cannot be found in tree.");
    }
    final RevisarTree<String> parent = value.getParent();
    if (parent ==  null) {
      return false;
    }
    final int index = parent.getChildren().indexOf(value);
    return parent.getValue().equals(label) && index == 0;
  }
}
