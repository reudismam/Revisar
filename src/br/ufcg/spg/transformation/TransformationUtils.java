package br.ufcg.spg.transformation;

import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.config.TechniqueConfig;
import br.ufcg.spg.database.ClusterDao;
import br.ufcg.spg.database.TransformationDao;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.ml.clustering.DbScanClustering;
import br.ufcg.spg.ml.editoperation.Script;
import br.ufcg.spg.refaster.RefasterTranslator;
import br.ufcg.spg.validator.ClusterValidator;
import br.ufcg.spg.validator.RenameChecker;
import br.ufcg.spg.validator.node.INodeChecker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class TransformationUtils {
  private static List<Script> scripts = new ArrayList<>();
  private static List<Script> renameScripts = new ArrayList<>();
  
  private static int clusterIndex = 1;
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
   * Computes the template for some cluster.
   * @param clusterId label of the cluster
   */
  public static void transformationsLargestClusters() {
    final List<Cluster> clusters = getLargestClusters();
    transformations(clusters);
  }
  
  /**
   * Computes the template for some cluster.
   * @param clusterId label of the cluster
   */
  public static void transformationsMoreProjects() {
    final List<Cluster> clusters = getClusterMoreProjects();
    transformations(clusters);
    List<ArrayList<Script>> clusteredScripts =  DbScanClustering.cluster(scripts);
    int countCluster = 0;
    List<Script> clusteredScriptsList = new ArrayList<>();
    for (ArrayList<Script> list : clusteredScripts) {
      clusteredScriptsList.addAll(list);
      saveCluster(++countCluster, list);
    }
    saveCluster(++countCluster, renameScripts);
    for (Script sc : scripts) {
      if (!clusteredScriptsList.contains(sc)) {
        String content = "EDITS\n";
        content += sc.getList() + "\n";
        content += "SRC CLUSTER\n";
        content += sc.getCluster() + "\n";
        content += "DST CLUSTER\n";
        content += sc.getCluster().getDst() + "\n";
        String path = "../Projects/cluster/clusters/" + ++ countCluster + ".txt";
        final File clusterFile = new File(path);
        try {
          FileUtils.writeStringToFile(clusterFile, content);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static void saveCluster(int countCluster, List<Script> list) {
    String content = "NUMBER OF NODES IN THIS CLUSTER: " + list.size() + "\n\n";
    for (Script sc : list) {
      content += "EDITS\n";
      content += sc.getList() + "\n";
      content += "SRC CLUSTER\n";
      content += sc.getCluster() + "\n";
      content += "DST CLUSTER\n";
      content += sc.getCluster().getDst() + "\n";
    }
    String path = "../Projects/cluster/clusters/" + countCluster + ".txt";
    final File clusterFile = new File(path);
    try {
      FileUtils.writeStringToFile(clusterFile, content);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Computes transformations for a set of clusters.
   */
  public static void transformations(final List<Cluster> srcClusters) {
    try {
      for (int i = 0; i < srcClusters.size(); i++) {
        System.out.println(((double) i) / srcClusters.size() + " % completed.");
        final Cluster clusteri = srcClusters.get(i);
        // Analyze clusters with two or more elements.
        if (clusteri.getNodes().size() < 2) {
          continue;
        }
        Edit edit = clusteri.getNodes().get(0);
        Transformation transformation = tranformation(clusteri, edit);
        TransformationDao.getInstance().save(transformation);
        saveTransformation(transformation);
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Learns a transformation for a cluster.
   */
  public static Transformation tranformation(final Cluster clusteri, final Edit srcEdit) {
    try {
      String refaster;
      if (TechniqueConfig.getInstance().isCreateRule()) {    
        refaster = RefasterTranslator.translate(clusteri, srcEdit);
      } else {
        refaster = "NOT SET";
      }
      final boolean isValid = ClusterValidator.isValidTrans(clusteri);
      final Transformation trans = new Transformation();
      trans.setTransformation(refaster);
      trans.setCluster(clusteri);
      trans.setValid(isValid);
      return trans;
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Saves a transformation.
   */
  public static void saveTransformation(final Transformation trans) throws IOException {
    String refaster = trans.getTransformation();
    Cluster clusteri = trans.getCluster();
    Cluster clusterj = clusteri.getDst();
    String content = "";
    content += refaster + "\n";
    content += "SRC CLUSTER\n";
    content += clusteri + "\n";
    content += "DST CLUSTER\n";
    content += clusterj + "\n";
    final INodeChecker ch = new RenameChecker(clusteri, clusterj);
    boolean isRename = false;
    try {
      isRename = ch.isValidUnification();
    } catch (final Exception e) {
      e.printStackTrace();
    }
    
    boolean isSameBeforeAfter = isSameBeforeAfter(clusteri);
    String path;
    if (isSameBeforeAfter) {
      path = "../Projects/cluster/same/" + clusteri.getId() + ".txt";
      return;
    }
    
    Script script = DbScanClustering.getCluster(clusteri);    
    String beforePattern = "VARIABLE_DECLARATION_FRAGMENT\\(SIMPLE_NAME\\(hash_[0-9]+\\)\\)";
    String afterPattern = "VARIABLE_DECLARATION_FRAGMENT\\(SIMPLE_NAME\\([a-zA-Z0-9_]+\\)\\)";
    String returnBp = "RETURN_STATEMENT\\(SIMPLE_NAME\\(hash_[0-9]\\)\\)";
    String returnAp = "RETURN_STATEMENT\\(SIMPLE_NAME\\([a-zA-Z0-9_]+\\)\\)";
    final String srcOutput =  clusteri.getAu();
    final String dstOutput = clusterj.getAu();
    if ((srcOutput.matches(beforePattern) && dstOutput.matches(afterPattern))
        || srcOutput.matches(returnBp) && dstOutput.matches(returnAp)) {
      renameScripts.add(script);
      return;
    }
      
    if (!script.getList().isEmpty()) {
      scripts.add(script);
    }
    if (isRename) {
      path = "../Projects/cluster/rename/" + clusterIndex++ + ".txt";
    } else {
      path = "../Projects/cluster/" + trans.isValid() + "/" + clusterIndex++ + ".txt";
    }
    final File clusterFile = new File(path);
    FileUtils.writeStringToFile(clusterFile, content);
  }

  private static List<Cluster> getClusters() {
    final ClusterDao dao = ClusterDao.getInstance();
    final List<Cluster> srcClusters = dao.getSrcClusters();
    return srcClusters;
  }
  
  public static boolean isSameBeforeAfter(final Cluster clusteri) {
    for (Edit c : clusteri.getNodes()) {
      Edit dstEdit = c.getDst();
      if (!c.getText().equals(dstEdit.getText())) {
        return false;
      }
    }
    return true;
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
    final List<Cluster> srcClusters = dao.getClusterMoreProjects(3);
    return srcClusters;
  }
}
