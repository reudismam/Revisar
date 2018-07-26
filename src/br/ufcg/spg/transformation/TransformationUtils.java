package br.ufcg.spg.transformation;

import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.config.TechniqueConfig;
import br.ufcg.spg.database.ClusterDao;
import br.ufcg.spg.database.TransformationDao;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.ml.clustering.DbScanClustering;
import br.ufcg.spg.ml.editoperation.Script;
import br.ufcg.spg.ml.metric.ScriptDistanceMetric;
import br.ufcg.spg.refaster.RefasterTranslator;
import br.ufcg.spg.string.StringUtils;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeParser;
import br.ufcg.spg.util.PrintUtils;
import br.ufcg.spg.validator.ClusterValidator;
import de.jail.geometry.schemas.Point;
import de.jail.statistic.clustering.density.DBScan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class to perform transformations.
 */
public final class TransformationUtils {
  
  /**
   * Learned scripts.
   */
  private static List<Point> scripts = new ArrayList<>();
  /**
   * Rename scripts.
   */
  private static List<Script> renameScripts = new ArrayList<>();
  
  /**
   * Logger.
   */
  private static final Logger logger = LogManager.getLogger(TransformationUtils.class.getName());
  
  /**
   * Field only for test purpose.
   */
  private static int clusterIndex = 1;
  
  private TransformationUtils() {
  }
  
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
   * Computes transformations for a set of clusters.
   */
  public static void transformations(final List<Cluster> srcClusters) {
    try {
      for (int i = 0; i < srcClusters.size(); i++) {
        logger.trace(((double) i) / srcClusters.size() + " % completed.");
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
      logger.error(e.getStackTrace());
    }
  }
  
  /**
   * Computes the template for some cluster.
   */
  public static void transformationsLargestClusters() {
    final List<Cluster> clusters = getLargestClusters();
    transformations(clusters);
  }
  
  /**
   * Computes the template for some cluster.
   */
  public static void transformationsMoreProjects(List<Cluster> clusters) {
    transformations(clusters);
    DBScan dbscan = new DBScan(0.01, 1, new ScriptDistanceMetric());
    List<de.jail.statistic.clustering.Cluster> clusteres = dbscan.cluster(scripts);
    int countCluster = 0;
    List<Script> clusteredScriptsList = new ArrayList<>();
    for (de.jail.statistic.clustering.Cluster list : clusteres) {
      List<Script> ls = new ArrayList<>();
      for (Point p : list.getAllPoints()) {
        Script sc = (Script) p;
        ls.add(sc);
      }
      clusteredScriptsList.addAll(ls);
      saveCluster(++countCluster, ls);
    }
    if (!renameScripts.isEmpty()) {
      saveCluster(++countCluster, renameScripts);
    }
    for (final Point point : scripts) {
      Script sc = (Script) point;
      Cluster clusteri = sc.getCluster();
      Cluster clusterj = clusteri.getDst();
      if (!clusteredScriptsList.contains(sc)) {
        StringBuilder content = new StringBuilder("");
        content.append(sc.getList()).append('\n');
        content.append(formatHeader());
        content.append(formatClusterContent(clusteri, clusterj));
        content.append(formatHeader());
        String counterFormated =  String.format("%03d", ++ countCluster);
        String path = "../Projects/cluster/clusters/" + counterFormated + ".txt";
        final File clusterFile = new File(path);
        try {
          FileUtils.writeStringToFile(clusterFile, content.toString());
        } catch (IOException e) {
          logger.error(e.getStackTrace());
        }
      }
    }
  }

  private static void saveCluster(int countCluster, List<Script> list) {
    StringBuilder content = new StringBuilder("NUMBER OF NODES IN THIS CLUSTER: " 
        + list.size()).append("\n\n");
    int count = 0;
    for (Script sc : list) {
      content.append(formatHeader());
      content.append(sc.getList()).append('\n');
      String cnumber = String.format("%03d", ++count);
      content.append("CLUSTER ").append(cnumber).append('\n');
      Cluster clusteri = sc.getCluster();
      Cluster clusterj = clusteri.getDst();
      content.append(formatClusterContent(clusteri, clusterj));
      content.append(formatFooter());
    }
    String counterFormated =  String.format("%03d", countCluster);
    String path = "../Projects/cluster/clusters/" + counterFormated + ".txt";
    final File clusterFile = new File(path);
    try {
      FileUtils.writeStringToFile(clusterFile, content.toString());
    } catch (IOException e) {
      logger.error(e.getStackTrace());
    }
  }

  private static String formatHeader() {
    StringBuilder content = new StringBuilder();
    content.append("================================================================================\n");
    content.append("=================================CLUSTER DATA===================================\n");
    content.append("================================================================================\n");
    return content.toString();
  }
  
  private static String formatFooter() {
    StringBuilder content = new StringBuilder();
    content.append("================================================================================\n");
    content.append("==============================END OF CLUSTER DATA===============================\n");
    content.append("================================================================================\n\n");
    return content.toString();
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
        refaster = "";
      }
      final boolean isValid = ClusterValidator.isValidTrans(clusteri);
      final Transformation trans = new Transformation();
      trans.setTransformation(refaster);
      trans.setCluster(clusteri);
      trans.setValid(isValid);
      return trans;
    } catch (final Exception e) {
      logger.error(e.getStackTrace());
    }
    return null;
  }

  /**
   * Saves a transformation.
   */
  public static void saveTransformation(final Transformation trans) throws IOException {
    final String refaster = trans.getTransformation();
    final Cluster clusteri = trans.getCluster();
    final Cluster clusterj = clusteri.getDst();
    StringBuilder content = new StringBuilder("");
    content.append(formatHeader());
    content.append(refaster).append('\n');
    content.append(formatClusterContent(clusteri, clusterj));
    content.append(formatHeader());
    //Script script = DbScanClustering.getCluster(clusteri);    
    if (isSameBeforeAfter(clusteri)) {
      return;
    }
    //scripts.add(script);
    String path = "../Projects/cluster/" + trans.isValid() + '/' + clusteri.getId() + ".txt";
    final File clusterFile = new File(path);
    FileUtils.writeStringToFile(clusterFile, content.toString());
  }
  
  private static String formatClusterContent(final Cluster clusteri, final Cluster clusterj) {
    StringBuilder content = new StringBuilder();
    RevisarTree<String> tempBefore = RevisarTreeParser.parser(clusteri.getAu());
    String before = PrintUtils.prettyPrint(tempBefore);
    RevisarTree<String> tempAfter = RevisarTreeParser.parser(clusterj.getAu());
    String after = PrintUtils.prettyPrint(tempAfter);
    String addToLines = "          ";
    String afterFormated = formatOutput(after, addToLines);
    String output = StringUtils.printStringSideBySide(before, afterFormated);
    content.append(output);
    content.append(formatStringNodes(clusteri.getNodes()));
    return content.toString();
  }
  
  private static String formatStringNodes(final List<Edit> srcNodes) {
    StringBuilder result = new StringBuilder();
    result.append("\nEXAMPLES IN THIS CLUSTER ").append(srcNodes.size()).append(":\n\n");
    StringBuilder beforeNodes = new StringBuilder();
    StringBuilder afterNodes = new StringBuilder();
    int count = 0;
    for (final Edit node : srcNodes) {
      beforeNodes.append(node.getText()).append('\n');
      afterNodes.append(node.getDst().getText()).append('\n');
      if (++count == 4) {
        break;
      }
    }
    String addToLines = "    >>    ";
    String afterOutput = formatOutput(afterNodes.toString(), addToLines);
    String output = StringUtils.printStringSideBySide(beforeNodes.toString(), afterOutput);
    result.append(output);
    result.append("...\n");
    return result.toString();
  }
  
  /**
   * Format output.
   * @param pattern pattern
   */
  private static String formatOutput(final String pattern, final String addToLines) {
    String newPattern = "";
    String addToMid = "    >>    ";
    String[] lines = pattern.split("\n");
    int mid = lines.length / 2;
    for (int i = 0; i < lines.length; i++) {
      if (i == mid) {
        newPattern += addToMid + lines[i] + '\n';
      } else {
        newPattern += addToLines + lines[i] + '\n';
      }
    }
    return newPattern;
  }

  private static List<Cluster> getClusters() {
    final ClusterDao dao = ClusterDao.getInstance();
    return dao.getSrcClusters();
  }
  
  private static boolean isSameBeforeAfter(final Cluster clusteri) {
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
    return dao.getLargestClusters();
  }
  
  /**
   * Get clusters with the largest number of examples.
   */
  public static List<Cluster> getClusterMoreProjects() {
    final ClusterDao dao = ClusterDao.getInstance();
    return dao.getClusterMoreProjects(3);
  }
}
