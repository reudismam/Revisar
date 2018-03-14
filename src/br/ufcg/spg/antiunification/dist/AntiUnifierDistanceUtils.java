package br.ufcg.spg.antiunification.dist;

import at.jku.risc.stout.urauc.algo.AntiUnifyProblem.VariableWithHedges;
import at.jku.risc.stout.urauc.data.Hedge;
import at.jku.risc.stout.urauc.data.atom.Variable;

import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.tree.RevisarTreeParser;
import br.ufcg.spg.tree.RevisarTree;

import java.util.HashSet;
import java.util.List;

public class AntiUnifierDistanceUtils {

  /**
   * Computes the distance inducted by the anti-unification. The ant-unification
   * template could be converted on the tree edit distance algorithm. Thus, the
   * distance between two trees is computed by calculating the distance between
   * two trees. This value is provided by the anti-unification algorithm
   */
  public static int distance(final AntiUnifier root) {
    if (root == null) {
      return 0;
    }
    int distUnification = 0;
    for (final VariableWithHedges var : root.getValue().getVariables()) {
      final Hedge left = var.getLeft();
      final Hedge right = var.getRight();
      final RevisarTree<String> treeLeft = RevisarTreeParser.parser(left.toString());
      final RevisarTree<String> treeRight = RevisarTreeParser.parser(right.toString());
      final int sizeLeft = DistUtil.computeSize(treeLeft);
      final int sizeRight = DistUtil.computeSize(treeRight);
      distUnification += sizeLeft + sizeRight;
    }
    final List<AntiUnifier> unifiers = root.getChildren();
    int dist = 0;
    for (final AntiUnifier unifier : unifiers) {
      dist += distance(unifier);
    }
    return distUnification + dist;
  }

  /**
   * Computes the number of placeholders in the anti unification.
   * 
   * @param root
   *          Anti-unification to be analyzed
   * @return The number of placeholders
   */
  public static HashSet<Variable> placeHolders(final AntiUnifier root) {
    if (root == null) {
      return new HashSet<>();
    }
    final HashSet<Variable> set = new HashSet<Variable>();
    for (final VariableWithHedges var : root.getValue().getVariables()) {
      final Variable variable = var.getVar();
      if (!set.contains(variable)) {
        set.add(variable);
      }
    }
    final List<AntiUnifier> children = root.getChildren();
    for (final AntiUnifier unifier : children) {
      final HashSet<Variable> hash = placeHolders(unifier);
      set.addAll(hash);
    }
    return set;
  }
}
