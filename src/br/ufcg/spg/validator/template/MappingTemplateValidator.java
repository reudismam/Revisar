package br.ufcg.spg.validator.template;

import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.antiunification.AntiUnifierUtils;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.match.Match;
import br.ufcg.spg.node.NodesExtractor;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;

/**
 * Checks mapping.
 */
public class MappingTemplateValidator implements ITemplateValidator {
  /**
   * Source code anti-unification.
   */
  private final String srcAu;
  /**
   * Destination code anti-unification.
   */
  private final String dstAu;
  /**
   * Edit list.
   */
  private final List<Edit> srcEdits;

  /**
   * Creates a new instance.
   */
  public MappingTemplateValidator(
      final String srcAu, final String dstAu, final List<Edit> srcEdits) {
    this.srcAu = srcAu;
    this.dstAu = dstAu;
    this.srcEdits = srcEdits;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValidUnification() {
    try {
      final Edit firstEdit = srcEdits.get(0);
      final Edit lastEdit = srcEdits.get(srcEdits.size() - 1);
      String templateCluster = "JOIN(" + srcAu + ", " + dstAu + ")";
      String templateEdit = "JOIN(" + lastEdit.getPlainTemplate() + ", "
          + lastEdit.getDst().getPlainTemplate() + ")";
      final AntiUnifier srcAu2 = AntiUnifierUtils.antiUnify(templateCluster, 
          templateEdit);
      final String srcUnifier2 = EquationUtils.convertToEquation(srcAu2);
      RevisarTree<String> tree = RevisarTreeParser.parser(srcUnifier2);
      RevisarTree<String> before = tree.getChildren().get(0);
      RevisarTree<String> after = tree.getChildren().get(1);
      List<String> beforeHoles = getHoles(before);
      List<String> afterHoles = getHoles(after);
      boolean valid = beforeHoles.containsAll(afterHoles);
      return valid;
      /*final List<Match> matchesFirst = getInputOuputMatches(firstEdit, srcAu, dstAu);
      final boolean sameSize = isHolesSameSize(firstEdit, lastEdit);
      if (!sameSize) {
        return false;
      }
      if (!isOuputSubstituingInInput(firstEdit, srcAu, dstAu)) {
        return false;
      }
      final List<Match> matchesLast = getInputOuputMatches(lastEdit, srcAu, dstAu);
      if (!isOuputSubstituingInInput(lastEdit, srcAu, dstAu)) {
        return false;
      }
      if (matchesFirst.size() != matchesLast.size()) {
        return false;
      }
      return isCompatible(matchesFirst, matchesLast);*/
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<String> getHoles(RevisarTree<String> before) {
    final Map<String, RevisarTree<String>> mapping = getStringRevisarTreeMapping(before);
    List<String> holes = new ArrayList<>();
    for (Entry<String, RevisarTree<String>> entry : mapping.entrySet()) {
      if (entry.getKey().matches("hash_[0-9]+")) {
        holes.add(entry.getKey());
      }
    }
    return holes;
  }

  /**
   * Verifies whether substituting nodes in output is present on input tree.
   */
  private boolean isOuputSubstituingInInput(
      final Edit srcEdit, final String srcAu, final String dstAu) {
    final Edit dstEdit = srcEdit.getDst();
    final String srcTemplate = srcEdit.getPlainTemplate();
    final Map<String, RevisarTree<String>> srcMapping = getStringRevisarTreeMapping(srcTemplate);
    final Set<String> dstSubstitutings = AntiUnifierUtils.getSubstitutings(dstEdit, dstAu);
    for (final String str : dstSubstitutings) {
      if (!srcMapping.containsKey(str)) {
        return false;
      }
    }
    final Map<String, String> holeSubstitutingsSrc = AntiUnifierUtils.getUnifierMatching(
        srcAu, srcTemplate);
    for (final String str : dstSubstitutings) {
      boolean isAbstractedInInput = false;
      for (final Entry<String, String> entry : holeSubstitutingsSrc.entrySet()) {
        if (entry.getValue().equals(str)) {
          isAbstractedInInput = true;
        }
      }
      if (!isAbstractedInInput) {
        return false;
      }
    }
    return true;
  }

  /**
   * Verifies whether two list of matches are compatible. Two list are
   * compatible if the have the same hole from before and after version.
   * 
   * @param matchesFirst
   *          first list of matches.
   * @param matchesLast
   *          second list of matches.
   */
  private boolean isCompatible(final List<Match> matchesFirst, final List<Match> matchesLast) {
    Set<Tuple<String, String>> set = new HashSet<>();
    for (final Match match : matchesFirst) {
      set.add(new Tuple<>(match.getSrcHash().trim(), match.getDstHash().trim()));
    }
    for (final Match match : matchesLast) {
      if (!set.contains(new Tuple<>(match.getSrcHash().trim(), match.getDstHash().trim()))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Verifies whether the size of holes when we anti-unify the first edit and
   * anti-unify the second edit is the same.
   */
  private boolean isHolesSameSize(final Edit first, final Edit last) {
    final Map<String, String> substutingsFirst = AntiUnifierUtils.getUnifierMatching(
        srcAu, first.getPlainTemplate());
    final Map<String, String> substitutingsLast = AntiUnifierUtils.getUnifierMatching(
        srcAu, last.getPlainTemplate());
    return substutingsFirst.size() == substitutingsLast.size();
  }

  private List<Match> getInputOuputMatches(
      final Edit srcEdit, final String srcAu, final String dstAu) {
    final Edit dstEdit = srcEdit.getDst();
    final String srcTemplate = srcEdit.getPlainTemplate();
    final String dstTemplate = dstEdit.getPlainTemplate();
    final Map<String, String> holeSubstitutingsSrc = AntiUnifierUtils.getUnifierMatching(
        srcAu, srcTemplate);
    final Map<String, String> holeSubstitutingsDst = AntiUnifierUtils.getUnifierMatching(
        dstAu, dstTemplate);
    Map<String, List<String>> substitutingHolesDst = reverse(holeSubstitutingsDst);
    final List<Match> matches = new ArrayList<>();
    for (final Entry<String, String> entry : holeSubstitutingsSrc.entrySet()) {
      if (substitutingHolesDst.containsKey(entry.getValue())) {
        List<String> dstKeys = substitutingHolesDst.get(entry.getValue());
        for (final String dstKey : dstKeys) {
          Match match = new Match(entry.getKey(), dstKey, entry.getValue());
          matches.add(match);
        }
      }
    }
    return matches;
  }

  /**
   * Invert the mapping.
   */
  public Map<String, List<String>> reverse(final Map<String, String> holeSubstutings) {
    final Map<String, List<String>> inverted = new HashMap<>();
    for (final Entry<String, String> entry : holeSubstutings.entrySet()) {
      if (!holeSubstutings.containsKey(entry.getValue())) {
        inverted.put(entry.getValue(), new ArrayList<>());
      }
      inverted.get(entry.getValue()).add(entry.getKey());
    }
    return inverted;
  }

  /**
   * Gets mapping between string and tree.
   * 
   * @param edit
   *          edit.
   * @return mapping between and tree.
   */
  private Map<String, RevisarTree<String>> getStringRevisarTreeMapping(final String template) {
    final RevisarTree<String> revisarTree = RevisarTreeParser.parser(template);
    return getStringRevisarTreeMapping(revisarTree);
  }
  
  /**
   * Gets mapping between string and tree.
   * 
   * @param edit
   *          edit.
   * @return mapping between and tree.
   */
  private Map<String, RevisarTree<String>> getStringRevisarTreeMapping(
      final RevisarTree<String> revisarTree) {
    final Map<String, RevisarTree<String>> mapping = new HashMap<>();
    final List<RevisarTree<String>> treeNodes = NodesExtractor.getNodes(revisarTree);
    for (final RevisarTree<String> node : treeNodes) {
      final String str = EquationUtils.convertToEq(node);
      mapping.put(str, node);
    }
    return mapping;
  }
}
