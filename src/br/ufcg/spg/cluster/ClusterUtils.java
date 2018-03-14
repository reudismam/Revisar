package br.ufcg.spg.cluster;

import java.io.IOException;

/**
 * Cluster Utils class.
 */
public final class ClusterUtils {
  private ClusterUtils() {
  }
  
  /**
   * Builds cluster data set.
   */
  public static void buildClusters() {
    final UnifierCluster unifierCluster = UnifierCluster.getInstance();
    try {
      unifierCluster.clusters();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
