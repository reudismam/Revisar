package br.ufcg.spg.replacement;

import br.ufcg.spg.analyzer.util.AnalyzerUtil;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.compile.CompilerUtils;
import br.ufcg.spg.diff.DiffCalculator;
import br.ufcg.spg.diff.DiffUtils;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.matcher.AbstractMatchCalculator;
import br.ufcg.spg.matcher.PositionMatchCalculator;
import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.project.Version;
import br.ufcg.spg.template.TemplateUtils;
import br.ufcg.spg.tree.AParser;
import br.ufcg.spg.tree.ATree;
import br.ufcg.spg.tree.ITreeParser;

import com.github.gumtreediff.tree.ITree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;

public class ReplacementUtils {
  public static final String REGEX = "(hash.\\d+)";
  
  /**
   * Calculates the replacements.
   * @param edit edit
   * @param au anti-unification
   * @return replacements
   */
  public static List<Replacement<ASTNode>> replacements(final Edit edit, final String au, 
      final CompilationUnit unit) 
      throws IOException, NoFilepatternException, GitAPIException {
    final AbstractMatchCalculator mcalc = new PositionMatchCalculator(edit.getStartPos(), 
        edit.getEndPos());
    final ASTNode node = mcalc.getNode(unit);
    final Map<String, List<ASTNode>> map = getAbstractionMap(au, node);
    final List<Replacement<ASTNode>> targetList = new ArrayList<>();
    if (!map.isEmpty()) {
      for (final String key : map.keySet()) {
        final List<ASTNode> values = map.get(key);
        final ASTNode last = values.get(values.size() - 1);
        final Replacement<ASTNode> replacement = new Replacement<>(key, last);
        targetList.add(replacement);
      }
    } else {
      final Replacement<ASTNode> replacement = new Replacement<>(edit.getPlainTemplate(), node);
      targetList.add(replacement);
    }
    return targetList;
  }
  
  /**
   * Calculates the replacements.
   * @param edit edit
   * @param au anti-unification
   * @return replacements
   */
  public static List<Replacement<String>> replacements(final Edit edit, final String au) 
      throws IOException, NoFilepatternException, GitAPIException {
    //return targetList;
    return null;
  }
  
  private static Map<String, List<ASTNode>> getAbstractionMap(final String clAu, final ASTNode node) {
    final ATree<String> template = AParser.parser(clAu);
    final ATree<String> au = TemplateUtils.removeAll(template);
    final ATree<Tuple<ASTNode, String>> parsed = ITreeParser.parse(au, node);
    final List<ATree<Tuple<ASTNode, String>>> nodes = AnalyzerUtil.getNodes(parsed);
    final Map<String, List<ASTNode>> map = new Hashtable<>();
    for (final ATree<Tuple<ASTNode, String>> tuple : nodes) {
      final ASTNode astNode = tuple.getValue().getItem1();
      final String item1 = tuple.getValue().getItem2();
      final Pattern pattern = Pattern.compile(REGEX);
      final Matcher matcher = pattern.matcher(item1);
      while (matcher.find()) {
        final String matchElement = item1.substring(matcher.start(), matcher.end());
        if (!map.containsKey(matchElement)) {
          map.put(matchElement, new ArrayList<>());
        }
        map.get(matchElement).add(astNode);
      }
    }
    return map;
  }
  
  /**
   * Gets before and after lists.
   */
  public static Tuple<List<ASTNode>, List<ASTNode>> mapping(
      final List<Replacement<ASTNode>> src, final List<Replacement<ASTNode>> dst, 
      final DiffCalculator diff, final CompilationUnit dstCu) {
    final List<ASTNode> befores = new ArrayList<>();
    final List<ASTNode> afters = new ArrayList<>();
    addNodePairsBasedOnGumTree(src, diff, dstCu, befores, afters);
    addNodePairsBasedOnStructure(src, dst, befores, afters);
    return new Tuple<List<ASTNode>, List<ASTNode>>(befores, afters);
  }

  /**
   * Adds pairs of edits based on node structure.
   * @param src source replacement
   * @param diff destination replacement
   * @param dstCu destination compilation unit
   * @param befores list of nodes abstracted for before version
   * @param afters list of nodes abstracted for after version
   */
  private static void addNodePairsBasedOnStructure(final List<Replacement<ASTNode>> src,
      final List<Replacement<ASTNode>> dst, final List<ASTNode> befores, final List<ASTNode> afters) {
    for (int i = 0; i < befores.size(); i++) {
      final Replacement<ASTNode> replacement = src.get(i);
      final ASTNode srcNode = replacement.getNode();
      if (afters.get(i) != null) {
        continue;
      }
      for (final Replacement<ASTNode> dstRepl : dst) {
        final ASTNode dstNode = dstRepl.getNode();
        if (srcNode.toString().trim().equals(dstNode.toString().trim())) {
          afters.set(i, dstNode);
        }
      }
    }
  }

  /**
   * Adds pairs of edits based on Gumtree mapping.
   * @param src source replacement
   * @param diff destination replacement
   * @param dstCu destination compilation unit
   * @param befores list of nodes abstracted for before version
   * @param afters list of nodes abstracted for after version
   */
  private static void addNodePairsBasedOnGumTree(final List<Replacement<ASTNode>> src,
      final DiffCalculator diff, final CompilationUnit dstCu, final List<ASTNode> befores,
      final List<ASTNode> afters) {
    for (final Replacement<ASTNode> replacement : src) {
      final ASTNode srcNode = replacement.getNode();
      if (!replacement.isUnification()) {
        continue;
      }
      final ITree srcRoot = diff.getSrc().getRoot();
      AbstractMatchCalculator mcalc = new PositionMatchCalculator(srcNode);
      final ITree itreeSrc = mcalc.getNode(srcRoot);
      final ITree itreeDst = diff.getMatcher().getMappings().getDst(itreeSrc);
      befores.add(srcNode);
      if (itreeDst != null) {
        mcalc = new PositionMatchCalculator(itreeDst);
        final ASTNode dstNode = mcalc.getNode(dstCu);
        if (srcNode.toString().trim().equals(dstNode.toString().trim())) {
          afters.add(dstNode);
        } else {
          afters.add(null);
        }
      } else {
        afters.add(null);
      }
    }
  }

  /**
   * Before and after list of edits.
   * @param srcEdit source edit
   * @param dstEdit destination edit
   * @param pi project information
   * @param src source code
   * @param dst destination code
   * @return before and after pairs of edits.
   */
  public static Tuple<List<ASTNode>, List<ASTNode>> ba(final Edit srcEdit,
      final Edit dstEdit, final ProjectInfo pi, final List<Replacement<ASTNode>> src,
      final List<Replacement<ASTNode>> dst) 
          throws IOException, NoFilepatternException, GitAPIException, ExecutionException {
    final DiffCalculator diff = DiffUtils.diff(srcEdit, dstEdit);
    final Version dstv = pi.getDstVersion();
    final String dstCommit = dstEdit.getCommit();
    final CompilationUnit dstCu = CompilerUtils.getCunit(dstEdit, dstCommit, dstv, pi);
    final Tuple<List<ASTNode>, List<ASTNode>> ma = mapping(src, dst, diff, dstCu);
    return ma;
  }
}
