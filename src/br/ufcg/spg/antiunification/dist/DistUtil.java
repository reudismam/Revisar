package br.ufcg.spg.antiunification.dist;

import br.ufcg.spg.tree.ATree;

public class DistUtil {
  /**
   * Converts a ASTNode to an anti-unification equation.
   */
  public static int computeSize(final ATree<String> au) {
    if (au.getChildren().isEmpty()) {
      return 1;
    }
    int sum = 0;
    for (int i = 0; i < au.getChildren().size(); i++) {
      sum += computeSize(au.getChildren().get(i));
    }
    return sum;
  }
}
