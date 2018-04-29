package br.ufcg.spg.validator.template;

import at.jku.risc.stout.urauc.algo.AntiUnifyProblem.VariableWithHedges;
import at.jku.risc.stout.urauc.data.Hedge;
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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Checks mapping.
 */
public class SimpleTypeTemplateChecker implements ITemplateChecker {
  /**
   * Source code anti-unification.
   */
  private transient String srcAu;
  /**
   * Edit list.
   */
  private transient List<Edit> srcEdits;
  
  /**
   * Creates a new instance.
   */
  public SimpleTypeTemplateChecker(final String srcAu, 
      final String dstAu, final List<Edit> srcEdits) {
    this.srcAu = srcAu;
    this.srcEdits = srcEdits;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean check() {
    final Edit firstEdit = srcEdits.get(0);
    final Map<String, String> substutingsFirst = AntiUnificationUtils.getUnifierMatching(firstEdit.getTemplate(), srcAu);
    final AntiUnifier unifier = UnifierCluster.computeUnification(firstEdit.getTemplate(), srcAu);
    final RevisarTree<String> tree = unifier.toRevisarTree();
    for (final Entry<String, String> match : substutingsFirst.entrySet()) {
      final String valueKey = "#" + match.getKey().substring(5);
      final IMatcher<RevisarTree<String>> matcher = new ValueTemplateMatcher(valueKey);
      final MatchCalculator<RevisarTree<String>> calc = new RevisarTreeMatchCalculator<>(matcher);
      final RevisarTree<String> value = calc.getNode(tree);
      final RevisarTree<String> parent = value.getParent();
      if (parent == null) {
        return false;
      }
      final RevisarTree<String> parentParent = parent.getParent();
      if (parentParent == null) {
        return false;
      }
      final String simpleTypeLabel = AnalyzerUtil.getLabel(ASTNode.SIMPLE_TYPE);
      if (parentParent.getValue().equals(simpleTypeLabel)) {
        return true;
      }
    }
    return false;
  }
}
