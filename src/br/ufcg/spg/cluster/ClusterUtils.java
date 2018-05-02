package br.ufcg.spg.cluster;

import br.ufcg.spg.database.ClusterDao;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.transformation.TransformationUtils;

import java.io.IOException;
import java.util.List;

/**
 * Cluster utility class.
 */
public final class ClusterUtils {
  private ClusterUtils() {
  }
  
  /**
   * Builds cluster data set.
   */
  public static void buildClusters() {
    final ClusterUnifier unifierCluster = ClusterUnifier.getInstance();
    try {
      unifierCluster.clusters();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Builds cluster given a cluster id.
   */
  public static void buildClusters(String clusterId) {
    ClusterDao dao = ClusterDao.getInstance();
    List<Cluster> clusters = dao.getClusters(clusterId);
    List<Edit> srcEdits = clusters.get(0).getNodes();
    final ClusterUnifier unifierCluster = ClusterUnifier.getInstance();
    try {
      List<Cluster> srcClusters = unifierCluster.clusterEdits(srcEdits);
      TransformationUtils.transformations(srcClusters);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
