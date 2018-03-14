package br.ufcg.spg.antiunification.dist;

import at.jku.risc.stout.urauc.algo.AntiUnifyProblem.VariableWithHedges;
import at.jku.risc.stout.urauc.data.Hedge;
import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.tree.AParser;
import br.ufcg.spg.tree.ATree;

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
    for (final VariableWithHedges var : root.getValue().getVariables()) {
      final Hedge hedge = getUnifier(var);
      final ATree<String> tree = AParser.parser(hedge.toString());
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
  
  public abstract Hedge getUnifier(VariableWithHedges root);
}
