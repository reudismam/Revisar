package br.ufcg.spg.validator.template;

import at.jku.risc.stout.urauc.algo.AntiUnifyProblem.VariableWithHedges;
import at.jku.risc.stout.urauc.data.Hedge;
import br.ufcg.spg.analyzer.util.AnalyzerUtil;
import br.ufcg.spg.antiunification.AntiUnificationUtils;
import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.cluster.UnifierCluster;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.match.Match;
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
    final Map<String, String> substutingsFirst = AntiUnificationUtils.getUnifierMatching(
        first.getTemplate(), srcAu);
    final Map<String, String> substitutingsLast = AntiUnificationUtils.getUnifierMatching(
        last.getTemplate(), srcAu);
    return substutingsFirst.size() == substitutingsLast.size();
  }
  
  private List<Match> getInputOuputMatching(final Edit srcEdit, 
      final String srcAu, final String dstAu) {
    final Edit dstEdit = srcEdit.getDst();
    final String srcTemplate = srcEdit.getPlainTemplate();
    final String dstTemplate = dstEdit.getPlainTemplate();
    final Map<String, String> holeSubstitutingsSrc = AntiUnificationUtils.getUnifierMatching(
        srcTemplate, srcAu);
    final Map<String, String> holeSubstitutingsDst = AntiUnificationUtils.getUnifierMatching(
        dstTemplate, dstAu);
    BiMap<String, String> substitutingHolesDst = HashBiMap.create(holeSubstitutingsDst).inverse();
    final List<Match> matches = new ArrayList<>();
    for (final Entry<String, String> entry : holeSubstitutingsSrc.entrySet()) {
      if (substitutingHolesDst.containsKey(entry.getValue())) {
        String dstKey = substitutingHolesDst.get(entry.getValue());
        Match match = new Match(entry.getKey(), dstKey, entry.getValue());
        matches.add(match);
      }
    }
    /*for (final Entry<String, String> srcEntry  : holeSubstutingSrc.entrySet()) {
      final String srcKey = srcEntry.getKey();
      final String srcValue = srcEntry.getValue();
      for (final Entry<String, String> dstEntry: holeSubstutingDst.entrySet()) {
        final String dstKey = dstEntry.getKey();
        final String dstValue = dstEntry.getValue();
        if (srcValue.equals(dstValue)) {
          final Match match = new Match(srcKey, dstKey, srcValue);
          matches.add(match);
        }
      }
    }*/
    return matches;
  }

  /**
   * Gets variable matching.
   * @param abstracted abstracted matching.
   * @param srcDstMapping source destination mapping.
   * @return destination variable matching.
   */
  public Map<String, RevisarTree<String>> getVariableMatching(
      final Map<String, RevisarTree<String>> abstracted,
      final Map<String, RevisarTree<String>> srcDstMapping) {
    final Map<String, RevisarTree<String>> variableMatching = new Hashtable<>();
    for (final Entry<String, RevisarTree<String>> entry : srcDstMapping.entrySet()) {
      final RevisarTree<String> value = entry.getValue();
      for (final Entry<String, RevisarTree<String>> abs : abstracted.entrySet()) {
        final RevisarTree<String> absValue = abs.getValue();
        if (isIntersect(value, absValue)) {
          variableMatching.put(abs.getKey(), value);
        }
      }
    }
    return variableMatching;
  }

  /**
   * Gets abstraction mapping.
   */
  public Map<String, RevisarTree<String>> getAbstractionMapping(final String template, 
      final Map<String, String> unifierMatching) {
    final Map<String, RevisarTree<String>> abstracted = new Hashtable<>();
    final RevisarTree<String> absTemplate = RevisarTreeParser.parser(template);
    final List<RevisarTree<String>> dstTreeNodes = AnalyzerUtil.getNodes(absTemplate); 
    for (final Entry<String, String> entry: unifierMatching.entrySet()) {
      for (final RevisarTree<String> dstNode : dstTreeNodes) {
        final String absNode = EquationUtils.convertToEq(dstNode);
        final String key = entry.getKey();
        final String value = entry.getValue();
        if (value.equals(absNode)) {
          abstracted.put(key, dstNode);
        }
      }
    }
    return abstracted;
  }

  /**
   * Gets mapping between string and tree.
   * @param edit edit.
   * @return mapping between and tree.
   */
  private Map<String, RevisarTree<String>> getStringRevisarTreeMapping(final String template) {
    final Map<String, RevisarTree<String>> mapping = new Hashtable<>();
    final RevisarTree<String> revisarTree = RevisarTreeParser.parser(template);
    final List<RevisarTree<String>> treeNodes = AnalyzerUtil.getNodes(revisarTree);
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
    final AntiUnifier unifier = UnifierCluster.computeUnification(au, edit.getPlainTemplate());
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
  
  /**
   * Verifies if the two nodes intersects.
   * @param root root node
   * @param toVerify to verify node
   */
  private boolean isIntersect(final RevisarTree<String> root, final RevisarTree<String> toVerify) {
    return isStartInside(root, toVerify) || isStartInside(toVerify, root);
  }
  
  /**
   * Verify if start position of toVerify is inside root.
   * @param root root node.
   * @param toVerify to verify.
   */
  private boolean isStartInside(final RevisarTree<String> root, 
      final RevisarTree<String> toVerify) {
    final int toVerifyStart = toVerify.getPos();
    final int rootStart = root.getPos();
    final int rootEnd = root.getEnd();
    return rootStart <= toVerifyStart && toVerifyStart <= rootEnd;   
  }
}
