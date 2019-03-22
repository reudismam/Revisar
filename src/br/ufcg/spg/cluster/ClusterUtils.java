package br.ufcg.spg.cluster;

import at.unisalzburg.dbresearch.apted.node.StringNodeData;

import br.ufcg.spg.cli.PatternStatus;
import br.ufcg.spg.cli.PatternUtils;
import br.ufcg.spg.database.ClusterDao;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.excel.QuickFix;
import br.ufcg.spg.main.MainArguments;
import br.ufcg.spg.ml.editoperation.Script;
import br.ufcg.spg.transformation.TransformationUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jgit.api.errors.GitAPIException;

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
    List<Cluster> srcClusters = clusterEdits(clusterId);
    TransformationUtils.transformations(srcClusters);
  }
  
  /**
   * Builds cluster given a cluster id.
   */
  public static List<Cluster> buildClustersSegmentByType(String clusterId) throws 
      BadLocationException, IOException, GitAPIException {
    List<Cluster> srcClusters = clusterEdits(clusterId);
    final List<Cluster> clusterList = new ArrayList<>();
    for (Cluster cluster : srcClusters) {
      List<Cluster> cls = segmentByType(cluster);
      clusterList.addAll(cls);
    }
    return clusterList;
  }

  /**
   * EFFECTs clusters the edits from a cluster
   * MODIFIES nothing
   */
  private static List<Cluster> clusterEdits(String clusterId) {
    ClusterDao dao = ClusterDao.getInstance();
    List<Cluster> clusters = dao.getClusters(clusterId);
    List<Edit> srcEdits = clusters.get(0).getNodes();
    final ClusterUnifier unifierCluster = ClusterUnifier.getInstance();
    return unifierCluster.clusterEdits(srcEdits);
  }

  /**
   * Segment by type.
   * @param srcCluster source cluster.
   */
  public static List<Cluster> segmentByType(Cluster srcCluster) throws 
      BadLocationException, IOException, GitAPIException {
    final Map<String, Cluster> map = new HashMap<>();
    for (final Edit edit : srcCluster.getNodes()) {
      final String refaster = TransformationUtils.createRefasterRule(srcCluster, edit);
      if (!map.containsKey(refaster)) {
        final String srcAu = srcCluster.getAu();
        final String srcLabel = srcCluster.getLabel();
        final Cluster dstCluster = srcCluster.getDst();
        final String dstAu = dstCluster.getAu();
        final String dstLabel = dstCluster.getLabel();
        final Cluster newCluster = new Cluster(srcAu, srcLabel);
        final Cluster newDstCluster = new Cluster(dstAu, dstLabel);
        newCluster.setDst(newDstCluster);
        map.put(refaster, newCluster);
      }
      final Cluster cluster = map.get(refaster);
      cluster.getNodes().add(edit);
      cluster.getDst().getNodes().add(edit.getDst());
    }
    return new ArrayList<>(map.values());
  }

  /**
   * Save cluster to file.
   */
  public static void saveClusterToFile(
      int countCluster, String folder, 
      List<Script<StringNodeData>> list, List<QuickFix> quicks) {
    StringBuilder content = new StringBuilder("NUMBER OF ITEMS IN THIS CLUSTER: " 
        + list.size()).append("\n\n");
    for (Script<StringNodeData> sc : list) {
      content.append(ClusterFormatter.getInstance().formatHeader());
      content.append(ClusterFormatter.formatList(sc.getList())).append('\n');
      String cnumber = String.format("%03d", TransformationUtils.incrementClusterIndex());
      content.append("CLUSTER ").append(cnumber).append('\n');
      Cluster clusteri = sc.getCluster();
      Cluster clusterj = clusteri.getDst();
      content.append(ClusterFormatter.getInstance().formatClusterContent(clusteri, clusterj));
      content.append(ClusterFormatter.getInstance().formatFooter());
      QuickFix qf = new QuickFix();
      qf.setId(TransformationUtils.getClusterIndex());
      qf.setCluster(clusteri);
      quicks.add(qf);
    }
    saveToFile(folder, countCluster, content);
  }
  
  /**
   * Save single clusters.
   */
  public static void saveSingleClusters(String folder, List<Cluster> clusters) {
    int countCluster = 0;
    for (final Cluster clusteri : clusters) {
      Cluster clusterj = clusteri.getDst();
      StringBuilder content = formatCluster(clusteri, clusterj);
      saveToFile(folder, ++countCluster, content);
    }
  }

  /**
   * Save single clusters.
   */
  public static void saveSingleQuickFixes(String folder, List<QuickFix> clusters) {
    for (final QuickFix quick : clusters) {
      Cluster clusteri = quick.getCluster();
      Cluster clusterj = clusteri.getDst();
      StringBuilder content = formatCluster(clusteri, clusterj);
      saveToFile(folder, quick.getId(), content);
    }
  }

  private static void saveToFile(String folder, int id, StringBuilder content) {
    String counterFormated = String.format("%03d", id);
    MainArguments main = MainArguments.getInstance();
    String path = main.getProjectFolder() + "/cluster/clusters/" + folder + counterFormated + ".txt";
    final File clusterFile = new File(path);
    try {
      FileUtils.writeStringToFile(clusterFile, content.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static StringBuilder formatCluster(Cluster clusteri, Cluster clusterj) {
    StringBuilder content = new StringBuilder();
    content.append(ClusterFormatter.getInstance().formatHeader());
    content.append(ClusterFormatter.getInstance().formatClusterContent(clusteri, clusterj));
    content.append(ClusterFormatter.getInstance().formatFooter());
    return content;
  }

  /**
   * Get all edits from provided clusters.
   */
  public static List<Edit> getAllEdits(List<Cluster> clusters) {
    List<Edit> allEdits = new ArrayList<>();
    for (Cluster c : clusters) {
      allEdits.addAll(c.getNodes());
    }
    return allEdits;
  }
}
