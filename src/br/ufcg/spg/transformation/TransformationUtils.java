package br.ufcg.spg.transformation;

import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.cluster.ClusterFormatter;
import br.ufcg.spg.config.TechniqueConfig;
import br.ufcg.spg.database.ClusterDao;
import br.ufcg.spg.database.TransformationDao;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.filter.PatternFilter;
import br.ufcg.spg.ml.editoperation.Script;
import br.ufcg.spg.ml.metric.ScriptDistanceMetric;
import br.ufcg.spg.refaster.RefasterTranslator;
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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jgit.api.errors.GitAPIException;

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
        /*if (!clusteri.getNodes().get(0).getText().contains(
            "new ArrayList<Task>(children.size())")) {
          continue;
        }*/
        // Analyze clusters with two or more elements.
        if (clusteri.getNodes().size() < 2) {
          continue;
        }
        Transformation transformation = tranformation(clusteri);
        //TransformationDao.getInstance().save(transformation);
        Edit edit = clusteri.getNodes().get(0);
        clusterIndex = i;
        saveTransformation(transformation, edit);
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Computes transformations for a set of clusters.
   */
  public static void transformations(final String folderPath,final List<Cluster> srcClusters) {
    try {
      for (int i = 0; i < srcClusters.size(); i++) {
        logger.trace(((double) i) / srcClusters.size() + " % completed.");
        final Cluster clusteri = srcClusters.get(i);
        /*if (!clusteri.getNodes().get(0).getText().contains(
            "new ArrayList<Task>(children.size())")) {
          continue;
        }*/
        // Analyze clusters with two or more elements.
        if (clusteri.getNodes().size() < 2) {
          continue;
        }
        Transformation transformation = tranformation(clusteri);
        //TransformationDao.getInstance().save(transformation);
        Edit edit = clusteri.getNodes().get(0);
        clusterIndex = i;
        saveTransformation(folderPath, transformation, edit);
      }
    } catch (final Exception e) {
      e.printStackTrace();
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
      saveClusterToFile(++countCluster, ls);
    }
    if (!renameScripts.isEmpty()) {
      saveClusterToFile(++countCluster, renameScripts);
    }
    for (final Point point : scripts) {
      Script sc = (Script) point;
      Cluster clusteri = sc.getCluster();
      Cluster clusterj = clusteri.getDst();
      if (!clusteredScriptsList.contains(sc)) {
        StringBuilder content = new StringBuilder("");
        content.append(sc.getList()).append('\n');
        content.append(ClusterFormatter.getInstance().formatHeader());
        content.append(ClusterFormatter.getInstance().formatClusterContent(clusteri, clusterj));
        content.append(ClusterFormatter.getInstance().formatFooter());
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

  private static void saveClusterToFile(int countCluster, List<Script> list) {
    StringBuilder content = new StringBuilder("NUMBER OF NODES IN THIS CLUSTER: " 
        + list.size()).append("\n\n");
    int count = 0;
    for (Script sc : list) {
      content.append(ClusterFormatter.getInstance().formatHeader());
      content.append(sc.getList()).append('\n');
      String cnumber = String.format("%03d", ++count);
      content.append("CLUSTER ").append(cnumber).append('\n');
      Cluster clusteri = sc.getCluster();
      Cluster clusterj = clusteri.getDst();
      content.append(ClusterFormatter.getInstance().formatClusterContent(clusteri, clusterj));
      content.append(ClusterFormatter.getInstance().formatFooter());
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
  
  /**
   * Learns a transformation for a cluster.
   */
  public static Transformation tranformation(final Cluster srcCluster) {
    try {     
      final boolean isValid = ClusterValidator.isValidTrans(srcCluster);
      final Transformation trans = new Transformation();
      trans.setCluster(srcCluster);
      trans.setValid(isValid);
      return trans;
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Create a Refaster rule.
   */
  public static String createRefasterRule(final Cluster srcCluster, 
      final Edit srcEdit)
      throws BadLocationException, IOException, GitAPIException {
    String refaster;
    if (TechniqueConfig.getInstance().isCreateRule()) {    
      try {
        refaster = RefasterTranslator.translate(srcCluster, srcEdit);
      } catch (Exception e) {
        refaster = "";
      }
    } else {
      refaster = "";
    }
    return refaster;
  }
  
  /**
   * Saves a transformation. 
   */
  public static void saveTransformation(final Transformation trans, final Edit edit) 
      throws IOException, BadLocationException, GitAPIException {
    String path = "../Projects/cluster/";
    saveTransformation(path, trans, edit);
  }

  /**
   * Saves a transformation. 
   */
  public static void saveTransformation(final String folderPath, 
      final Transformation trans, final Edit edit) 
      throws IOException, BadLocationException, GitAPIException {
    final Cluster clusteri = trans.getCluster();
    final Cluster clusterj = clusteri.getDst();
    //Script script = DbScanClustering.getCluster(clusteri);    
    if (isSameBeforeAfter(clusteri)) {
      return;
    } 
    List<PatternFilter> filters = filterFactory();
    for (PatternFilter filter : filters) {
      final String srcOutput =  clusteri.getAu();
      final String dstOutput = clusterj.getAu();
      if (filter.match(srcOutput, dstOutput)) {
        String counterFormated =  String.format("%03d", clusterIndex++);
        String path = folderPath + "filtered/" + trans.isValid() 
            + '/' + counterFormated + ".txt";
        final File clusterFile = new File(path);
        StringBuilder content = formatCluster(clusteri, clusterj, "");
        FileUtils.writeStringToFile(clusterFile, content.toString());
        return;
      }
    }  
    //Create rules only if the transformation is not a noise.
    final String refaster = createRefasterRule(clusteri, edit);
    trans.setTransformation(refaster);
    String counterFormated =  String.format("%03d", clusterIndex++);
    String path = folderPath + trans.isValid() + '/' + counterFormated + ".txt";
    final File clusterFile = new File(path);
    StringBuilder content = formatCluster(clusteri, clusterj, refaster);
    FileUtils.writeStringToFile(clusterFile, content.toString());
  }

  private static StringBuilder formatCluster(final Cluster clusteri, 
      final Cluster clusterj, final String refaster) {
    StringBuilder content = new StringBuilder("");
    content.append(ClusterFormatter.getInstance().formatHeader());
    content.append("Cluster ID: " + clusteri.getId()).append('\n');
    content.append(refaster).append('\n');
    content.append(ClusterFormatter.getInstance().formatClusterContent(clusteri, clusterj));
    content.append(ClusterFormatter.getInstance().formatFooter());
    return content;
  }
  
  private static List<PatternFilter> filterFactory() {
    PatternFilter varrename = new PatternFilter(
        "VARIABLE_DECLARATION_FRAGMENT\\(SIMPLE_NAME\\(hash_[0-9]+\\)\\)", 
        "VARIABLE_DECLARATION_FRAGMENT\\(SIMPLE_NAME\\([a-zA-Z0-9_]+\\)\\)");
    
    List<PatternFilter> pfilters = new ArrayList<>();
    
    PatternFilter trueFalse = new PatternFilter(
        "RETURN_STATEMENT\\([A-Z]+_[A-Z]+\\([a-zA-Z0-9_]+\\)\\)", 
        "RETURN_STATEMENT\\([A-Z]+_[A-Z]+\\([a-zA-Z0-9_]+\\)\\)");
    
    PatternFilter trueFalseVariable = new PatternFilter(
        "VARIABLE_DECLARATION_FRAGMENT\\(SIMPLE_NAME"
        + "\\([_0-9a-zA-Z]+\\), BOOLEAN_LITERAL\\([a-z]+\\)\\)", 
        "VARIABLE_DECLARATION_FRAGMENT\\(SIMPLE_NAME"
        + "\\([_0-9a-zA-Z]+\\), BOOLEAN_LITERAL\\([a-z]+\\)\\)");
    
    PatternFilter changeNumber = new PatternFilter(
        "VARIABLE_DECLARATION_FRAGMENT"
        + "\\(SIMPLE_NAME\\(hash_[0-9]+\\), NUMBER_LITERAL\\(hash_[0-9]+\\)\\)", 
        "VARIABLE_DECLARATION_FRAGMENT"
        + "\\(SIMPLE_NAME\\([_0-9A-Za-z]+\\), NUMBER_LITERAL\\([0-9]+\\)\\)");
    
    PatternFilter constructor = new PatternFilter(
        "SUPER_CONSTRUCTOR_INVOCATION\\([ _,a-zA-Z0-9\\(\\)]+\\)", 
        "SUPER_CONSTRUCTOR_INVOCATION\\([ _,a-zA-Z0-9\\(\\)]+\\)");
    
    PatternFilter swithcaseDefault = new PatternFilter(
        "SWITCH_CASE\\([\\(\\)_a-zA-Z0-9]+\\)", 
        "SWITCH_CASE\\([\\(\\)_a-zA-Z0-9]+\\)");
    
    PatternFilter markerAnnotationFilter = new PatternFilter(
        "MARKER_ANNOTATION\\(SIMPLE_NAME\\([a-zA-Z0-9_]+\\)\\)",
        "MARKER_ANNOTATION\\(SIMPLE_NAME\\([a-zA-Z0-9_]+\\)\\)");
   
    PatternFilter toSingleVariable = new PatternFilter(
        ".+", 
        "VARIABLE_DECLARATION_FRAGMENT\\(SIMPLE_NAME\\([a-zA-Z0-9_]+\\)\\)");
    
    PatternFilter infixes = new PatternFilter(
        "(INFIX_EXPRESSION|PREFIX_EXPRESSION|POSTFIX_EXPRESSION)\\([, a-zA-Z0-9\\)\\(_]+\\)", 
        "(INFIX_EXPRESSION|PREFIX_EXPRESSION|POSTFIX_EXPRESSION)\\([, a-zA-Z0-9\\)\\(_]+\\)");
    pfilters.add(varrename);
    pfilters.add(trueFalse);
    pfilters.add(changeNumber);
    pfilters.add(trueFalseVariable);
    pfilters.add(swithcaseDefault);
    pfilters.add(constructor);
    pfilters.add(markerAnnotationFilter);
    pfilters.add(toSingleVariable);
    pfilters.add(infixes);
    return pfilters;
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
