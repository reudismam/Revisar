package br.ufcg.spg.antiunification.dist;

import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.antiunification.substitution.HoleWithSubstutings;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeParser;
import java.util.List;

public abstract class DistanceCalculator {
  /**
   * Computes distance right-side.
   * @param root root node to be analyzed.
   * @return right-side distance
   */
  public int distance(final AntiUnifier root) {
    if (root == null) {
      return 0;
    }
    int distUnification = 0;
    for (final HoleWithSubstutings var : root.getValue().getVariables()) {
      final String hedge = getUnifier(var);
      final RevisarTree<String> tree = RevisarTreeParser.parser(hedge.toString());
      final int size = DistUtil.computeSize(tree);
      distUnification += size;
    }
    final List<AntiUnifier> children = root.getChildren();
    int cost = 0;
    for (final AntiUnifier unifier : children) {
      cost += distance(unifier);
    }
    return distUnification + cost;
  }
  
  public abstract String getUnifier(HoleWithSubstutings root);
}
