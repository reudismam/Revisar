package br.ufcg.spg.transformation;

import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.config.TechniqueConfig;
import br.ufcg.spg.database.ClusterDao;
import br.ufcg.spg.database.TransformationDao;
import br.ufcg.spg.refaster.RefasterTranslator;
import br.ufcg.spg.refaster.Transformation;
import br.ufcg.spg.validator.ClusterValidator;
import br.ufcg.spg.validator.RenameChecker;
import br.ufcg.spg.validator.node.INodeChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

public class TransformationUtils {
  /**
   * Computes the matches for all clusters.
   */
  public static void transformations() {
    final TransformationDao dao = TransformationDao.getInstance();
    final Long clusterId = dao.getLastClusterId();
    final List<Cluster> srcClusters = getClusters();
    final List<Cluster> remainingClusters = new ArrayList<>();
    if (clusterId == -1) {
      transformations(srcClusters);
    } else {
      boolean include = false;
      for (final Cluster cluster : srcClusters) {
        if (include) {
          remainingClusters.add(cluster);
        }
        if (cluster.getId().equals(clusterId)) {
          include = true;
        }
      }
      transformations(remainingClusters);
    }
  }
    
  /**
   * Computes the template for some cluster.
   * @param clusterId label of the cluster
   */
  public static void transformations(final String clusterId) {
    final ClusterDao dao = ClusterDao.getInstance();
    final List<Cluster> clusters = dao.getClusters(clusterId);
    transformations(clusters);
  }
  
  /**
   * Computes the matching between before and after variables.
   */
  public static void transformations(final List<Cluster> srcClusters) {
    try {
      for (int i = 0; i < srcClusters.size(); i++) {
        System.out.println(((double) i) / srcClusters.size() + " % completed.");
        final Cluster clusteri = srcClusters.get(i);
        final Cluster clusterj = clusteri.getDst();
        // Analyze clusters with two or more elements.
        if (clusteri.getNodes().size() < 2) {
          continue;
        }
        try {
          String refaster = "NOT SET";
          if (TechniqueConfig.getInstance().isCreateRule()) {    
            refaster = RefasterTranslator.translate(clusteri);
          }
          final Transformation trans = new Transformation();
          final boolean isValid = ClusterValidator.isValidTrans(clusteri);
          trans.setTransformation(refaster);
          trans.setCluster(clusteri);
          trans.setValid(isValid);
          TransformationDao.getInstance().save(trans);
          final INodeChecker ch = new RenameChecker(clusteri, clusterj);
          boolean isRename = false;
          try {
            isRename = ch.checkIsValidUnification();
          } catch (final Exception e) {
            e.printStackTrace();
          }
          String content = "";
          content += refaster + "\n";
          content += "SRC CLUSTER\n";
          content += clusteri + "\n";
          content += "DST CLUSTER\n";
          content += clusterj + "\n";
          String path;
          if (isRename) {
            path = "../Projects/cluster/rename/" + clusteri.getId() + ".txt";
          } else {
            path = "../Projects/cluster/" + isValid + "/" + clusteri.getId() + ".txt";
          }
          final File clusterFile = new File(path);
          FileUtils.writeStringToFile(clusterFile, content);
        } catch (final Exception e) {
          e.printStackTrace();
        }
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private static List<Cluster> getClusters() {
    final ClusterDao dao = ClusterDao.getInstance();
    final List<Cluster> srcClusters = dao.getSrcClusters();
    return srcClusters;
  }
  
  /**
   * Get clusters with the largest number of nodes.
   */
  public static List<Cluster> getLargestClusters() {
    final ClusterDao dao = ClusterDao.getInstance();
    final List<Cluster> srcClusters = dao.getLargestClusters();
    return srcClusters;
  }
  
  /**
   * Get clusters with the largest number of examples.
   */
  public static List<Cluster> getClusterMoreProjects() {
    final ClusterDao dao = ClusterDao.getInstance();
    final List<Cluster> srcClusters = dao.getClusterMoreProjects();
    return srcClusters;
  }
}
