package br.ufcg.spg.antiunification.cost;

import java.util.ArrayList;
import java.util.List;

import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.antiunification.AntiUnifierUtils;
import br.ufcg.spg.antiunification.dist.AntiUnifierDistanceUtils;
import br.ufcg.spg.antiunification.dist.LeftDistanceCalculator;
import br.ufcg.spg.antiunification.dist.RightDistanceCalculator;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.util.PrintUtils;

public final class CostUtils {
  
  private CostUtils() {
  }

  /**
   * Selects the best cluster. If not clusters if found, create a new one.
   * 
   * @param clusters
   *          - clusters
   * @param au
   *          - anti unification
   * @return best cluster
   */
  public static List<Tuple<String, Double>> cost(final List<Cluster> clusters, final String au) {
    final List<Tuple<String, Double>> cost = new ArrayList<>();
    for (final Cluster cluster : clusters) {
      final AntiUnifier unifier = AntiUnifierUtils.antiUnify(cluster.getAu(), au);
      final String unifierStr = EquationUtils.convertToEquation(unifier);
      final double clusterCost = CostUtils.computerAddCost(unifier);
      final Tuple<String, Double> tuple = new Tuple<>(unifierStr, clusterCost);
      cost.add(tuple);
    }
    return cost;
  }

  /**
   * Defines the add cost function.
   * 
   * @param root
   *          node
   * @return cost of unifying two trees
   */
  public static double computerAddCost(final AntiUnifier root) {
    final RevisarTree<String> atree = root.toRevisarTree();
    final String output = PrintUtils.prettyPrint(atree);
    if (output.contains("LARGER")) {
      return Double.POSITIVE_INFINITY;
    }
    final int size = atree.size();
    final int placeholders = AntiUnifierDistanceUtils.placeHolders(root).size();
    final LeftDistanceCalculator left = new LeftDistanceCalculator();
    final int leftSubstitutions = left.distance(root);
    final RightDistanceCalculator right = new RightDistanceCalculator();
    final int rightSubstitutions = right.distance(root);
    return ((double) (leftSubstitutions + rightSubstitutions) - placeholders) / size;
  }

}
