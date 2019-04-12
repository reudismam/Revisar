package br.ufcg.spg.antiunification.dist;

import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.antiunification.substitution.HoleWithSubstutings;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeParser;

import java.util.HashSet;
import java.util.List;

public class AntiUnifierDistanceUtils {
  
  private AntiUnifierDistanceUtils() {
  }

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
    for (final HoleWithSubstutings var : root.getValue().getVariables()) {
      final String left = var.getLeftSubstuting();
      final String right = var.getRightSubstuting();
      final RevisarTree<String> treeLeft = RevisarTreeParser.parser(left);
      final RevisarTree<String> treeRight = RevisarTreeParser.parser(right);
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
  public static HashSet<String> placeHolders(final AntiUnifier root) {
    if (root == null) {
      return new HashSet<>();
    }
    final HashSet<String> set = new HashSet<>();
    for (final HoleWithSubstutings var : root.getValue().getVariables()) {
      final String variable = var.getHole();
      if (!set.contains(variable)) {
        set.add(variable);
      }
    }
    final List<AntiUnifier> children = root.getChildren();
    for (final AntiUnifier unifier : children) {
      final HashSet<String> hash = placeHolders(unifier);
      set.addAll(hash);
    }
    return set;
  }
}
