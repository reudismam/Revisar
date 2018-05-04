package br.ufcg.spg.validator.template;

import at.jku.risc.stout.urauc.algo.AntiUnifyProblem.VariableWithHedges;
import at.jku.risc.stout.urauc.data.Hedge;
import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.antiunification.AntiUnifierUtils;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.cluster.ClusterUnifier;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.match.Match;
import br.ufcg.spg.node.NodesExtractor;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeParser;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
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
  private final transient String srcAu;
  /**
   * Destination code anti-unification.
   */
  private final transient String dstAu;  
  /**
   * Edit list.
   */
  private final transient List<Edit> srcEdits;
  
  /**
   * Creates a new instance.
   */
  public MappingTemplateValidator(final String srcAu, 
      final String dstAu, final List<Edit> srcEdits) {
    this.srcAu = srcAu;
    this.dstAu = dstAu;
    this.srcEdits = srcEdits;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValidUnification() {
    final Edit firstEdit = srcEdits.get(0);
    final List<Match> matchesFirst = getInputOuputMatching(firstEdit, srcAu, dstAu);
    final Edit lastEdit = srcEdits.get(srcEdits.size() - 1);
    final boolean sameSize = isHolesSameSize(firstEdit, lastEdit);
    if (!sameSize) {
      return false;
    }
    if (!isOuputSubstituingInInput(firstEdit, srcAu, dstAu)) {
      return false;
    }  
    final List<Match> matchesLast = getInputOuputMatching(lastEdit, srcAu, dstAu); 
    if (!isOuputSubstituingInInput(lastEdit, srcAu, dstAu)) {
      return false;
    }
    if (matchesFirst.size() != matchesLast.size()) {
      return false;
    }
    return isCompatible(matchesFirst, matchesLast);
  }
  
  /**
   * Verifies whether substituting nodes in output is present on input tree.
   */
  private boolean isOuputSubstituingInInput(final Edit srcEdit, 
      final String srcAu, final String dstAu) {
    final Edit dstEdit = srcEdit.getDst();
    final String srcTemplate = srcEdit.getPlainTemplate();
    final Map<String, RevisarTree<String>> srcMapping = getStringRevisarTreeMapping(srcTemplate);
    final Set<String> dstSubstitutings = getSubstitutings(dstEdit, dstAu);
    for (final String str: dstSubstitutings) {
      if (!srcMapping.containsKey(str)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Verifies whether two list of matches are compatible.
   * Two list are compatible if the have the same hole from
   * before and after version.
   * @param matchesFirst first list of matches.
   * @param matchesLast second list of matches.
   */
  private boolean isCompatible(final List<Match> matchesFirst, final List<Match> matchesLast) {
    Set<Tuple<String, String>> set = new HashSet<>();
    for (final Match match: matchesFirst) {
      set.add(new Tuple<>(match.getSrcHash().trim(), match.getDstHash().trim()));
    }
    for (final Match match: matchesLast) {
      if (!set.contains(new Tuple<>(match.getSrcHash().trim(), match.getDstHash().trim()))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Verifies whether the size of holes when we 
   * anti-unify the first edit and anti-unify the
   * second edit is the same.
   */
  private boolean isHolesSameSize(final Edit first, final Edit last) {
    final Map<String, String> substutingsFirst = AntiUnifierUtils.getUnifierMatching(
        srcAu, first.getPlainTemplate());
    final Map<String, String> substitutingsLast = AntiUnifierUtils.getUnifierMatching(
        srcAu, last.getPlainTemplate());
    return substutingsFirst.size() == substitutingsLast.size();
  }
  
  private List<Match> getInputOuputMatching(final Edit srcEdit, 
      final String srcAu, final String dstAu) {
    final Edit dstEdit = srcEdit.getDst();
    final String srcTemplate = srcEdit.getPlainTemplate();
    final String dstTemplate = dstEdit.getPlainTemplate();
    final Map<String, String> holeSubstitutingsSrc = AntiUnifierUtils.getUnifierMatching(
        srcAu, srcTemplate);
    final Map<String, String> holeSubstitutingsDst = AntiUnifierUtils.getUnifierMatching(
        dstAu, dstTemplate);
    BiMap<String, String> substitutingHolesDst = HashBiMap.create(holeSubstitutingsDst).inverse();
    final List<Match> matches = new ArrayList<>();
    for (final Entry<String, String> entry : holeSubstitutingsSrc.entrySet()) {
      if (substitutingHolesDst.containsKey(entry.getValue())) {
        String dstKey = substitutingHolesDst.get(entry.getValue());
        Match match = new Match(entry.getKey(), dstKey, entry.getValue());
        matches.add(match);
      }
    }
    return matches;
  }

  /**
   * Gets mapping between string and tree.
   * @param edit edit.
   * @return mapping between and tree.
   */
  private Map<String, RevisarTree<String>> getStringRevisarTreeMapping(final String template) {
    final Map<String, RevisarTree<String>> mapping = new Hashtable<>();
    final RevisarTree<String> revisarTree = RevisarTreeParser.parser(template);
    final List<RevisarTree<String>> treeNodes = NodesExtractor.getNodes(revisarTree);
    for (final RevisarTree<String> node : treeNodes) {
      final String str = EquationUtils.convertToEq(node);
      mapping.put(str, node);
    }
    return mapping;
  }

  /**
   * Get holes.
   */
  private Set<String> getSubstitutings(final Edit edit, final String au) {
    final Set<String> holes = new HashSet<>();
    final AntiUnifier unifier = ClusterUnifier.antiUnify(au, edit.getPlainTemplate());
    final List<VariableWithHedges> variables = unifier.getValue().getVariables();
    for (final VariableWithHedges variable : variables) {
      final String str = removeEnclosingParenthesis(variable.getRight());
      holes.add(str);
    }
    return holes;
  }

  /**
   * Remove parenthesis.
   * @param variable hedge variable
   * @return string without parenthesis
   */
  private String removeEnclosingParenthesis(final Hedge variable) {
    final String str = variable.toString().trim();
    final boolean startWithParen = str.startsWith("(");
    final boolean endWithParen = str.endsWith(")");
    if (!str.isEmpty() && startWithParen && endWithParen) {
      return str.substring(1, str.length() - 1);
    }
    return str;
  }
}
