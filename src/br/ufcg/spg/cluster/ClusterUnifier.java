package br.ufcg.spg.cluster;

import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.antiunification.AntiUnifierUtils;
import br.ufcg.spg.antiunification.cost.CostUtils;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.config.TechniqueConfig;
import br.ufcg.spg.database.ClusterDao;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.edit.EditStorage;
import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.transformation.TransformationUtils;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.validator.ClusterValidator;
import br.ufcg.spg.validator.node.NodeValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Unifier cluster class.
 */
public final class ClusterUnifier {
  /**
   * Number of clusters processed.
   */
  private int srcClusters;

  /**
   * Singleton instance.
   */
  private static ClusterUnifier instance;

  public static final Logger logger = LogManager.getLogger(ClusterUnifier.class.getName());

  private ClusterUnifier() {
  }

  /**
   * Gets the singleton instance of this class.
   * 
   * @return singleton instance
   */
  public static synchronized ClusterUnifier getInstance() {
    if (instance == null) {
      instance = new ClusterUnifier();
    }
    return instance;
  }

  /**
   * computes the clusters.
   */
  public void clusters() throws IOException {
    final EditStorage storage = EditStorage.getInstance();
    final TechniqueConfig config = TechniqueConfig.getInstance();
    final List<String> allDcaps = storage.getAllDCaps(config.getDcap());
    final ClusterDao dao = ClusterDao.getInstance();
    final int label = dao.getMaxLabel();
    srcClusters = label + 1;
    final String target = dao.getLastDcap(config.getDcap());
    int index = 0;
    if (target != null) {
      index = allDcaps.indexOf(target) + 1;
    }
    for (int d = index; d < allDcaps.size(); d++) {
      String logMessage = ((double) d) / allDcaps.size() + " % completed of all d-caps.\n";
      logger.trace(logMessage);
      Files.write(Paths.get("clustering.txt"), logMessage.getBytes(), StandardOpenOption.APPEND);
      final String dcap = allDcaps.get(d);
      final List<Edit> edits = storage.getSrcListByDCap(dcap, config.getDcap());
      if (edits.size() < 2) {
        continue;
      }
      final List<Cluster> clusters = clusters(edits);
      dao.saveAll(clusters);
      srcClusters += clusters.size();
      TransformationUtils.transformations(clusters);
    }
  }

  /**
   * Computes clusters.
   * 
   * @param srcList
   *          source clusters
   * @return clusters
   */
  public List<Cluster> clusters(final List<Edit> srcList) {
    final List<Cluster> src = new ArrayList<>();
    final TechniqueConfig config = TechniqueConfig.getInstance();
    final Map<String, List<Edit>> groups = groupEditsByDCap(srcList, config);
    for (final Entry<String, List<Edit>> entry : groups.entrySet()) {
      final List<Edit> toAnalyze = entry.getValue();
      final List<Cluster> clts = clusterEdits(toAnalyze);
      src.addAll(clts);
    }
    return src;
  }

  /**
   * Groups edits by d-cap.
   */
  public Map<String, List<Edit>> groupEditsByDCap(final List<Edit> srcList, 
      final TechniqueConfig config) {
    final Map<String, List<Edit>> groups = new HashMap<>();
    for (Edit edit : srcList) {
      String dcap = config.getDcap(edit.getDst());
      if (!groups.containsKey(dcap)) {
        groups.put(dcap, new ArrayList<>());
      }
      groups.get(dcap).add(edit);
    }
    return groups;
  }

  /**
   * computes the clusters.
   */
  public List<Cluster> clusterEdits(final List<Edit> srcList) {
    final List<Cluster> src = new ArrayList<>();
    final List<Cluster> dst = new ArrayList<>();
    logger.trace("CLUSTER LIST");
    if (srcList.isEmpty()) {
      return src;
    }
    for (int i = 0; i < srcList.size(); i++) {
      if (logger.isTraceEnabled()) {
        logger.trace(((double) i) / srcList.size() + "% complete for d-cap.");
      }
      final Edit srcEdit = srcList.get(i);
      if (!NodeValidator.isValidNode(srcEdit.getPlainTemplate())) {
        continue;
      }
      final Edit dstEdit = srcEdit.getDst();
      if (srcEdit.getPlainTemplate().contains(AntiUnifierUtils.LARGER)
          || dstEdit.getPlainTemplate().contains(AntiUnifierUtils.LARGER)) {
        continue;
      }
      try {
        final List<Tuple<Cluster, Double>> costs = bestCluster(src, dst, srcEdit, dstEdit);
        Cluster valid = searchForValid(srcEdit, dstEdit, costs);
        if (valid != null) {
          processValid(srcEdit, dstEdit, valid);
        } else {
          processInvalid(src, dst, srcEdit, dstEdit);
        }
      } catch (final Exception e) {
        e.printStackTrace();
      }
      if (logger.isTraceEnabled()) {
        logger.trace("DEBUG: " + (src.size() + srcClusters - 1));
      }
    }
    return src;
  }
  
  /**
   * computes the clusters.
   */
  public Tuple<Tuple<List<Cluster>, List<Cluster>>, Tuple<Edit, Edit>> clusterEditsAnalyzeInvalid(
      final List<Edit> srcList) {
    final List<Cluster> src = new ArrayList<>();
    final List<Cluster> dst = new ArrayList<>();
    int count = 0;
    logger.trace("CLUSTER LIST");
    if (srcList.isEmpty()) {
      return null;
    }
    for (int i = 0; i < srcList.size(); i++) {
      if (logger.isTraceEnabled()) {
        logger.trace(((double) i) / srcList.size() + "% complete for d-cap.");
      }
      final Edit srcEdit = srcList.get(i);
      if (!NodeValidator.isValidNode(srcEdit.getPlainTemplate())) {
        continue;
      }
      final Edit dstEdit = srcEdit.getDst();
      try {
        final List<Tuple<Cluster, Double>> costs = bestCluster(src, dst, srcEdit, dstEdit);
        Cluster valid = searchForValid(srcEdit, dstEdit, costs);
        if (valid != null) {
          processValid(srcEdit, dstEdit, valid);
        } else {
          if (count++ > 0) {
            return new Tuple<>(new Tuple<>(src, dst), new Tuple<>(srcEdit, dstEdit));
          } else {
            processInvalid(src, dst, srcEdit, dstEdit);
          }
        }
      } catch (final Exception e) {
        e.printStackTrace();
      }
      if (logger.isTraceEnabled()) {
        logger.trace("DEBUG: " + (src.size() + srcClusters - 1));
      }
    }
    return null;
  }

  /**
   * Searches for a valid cluster to receive the edits.
   */
  public Cluster searchForValid(
      final Edit srcEdit, final Edit dstEdit, final List<Tuple<Cluster, Double>> costs) {
    Cluster valid = null;
    for (final Tuple<Cluster, Double> tu : costs) {
      final Cluster srcCluster = tu.getItem1();
      if (isValid(srcCluster, srcCluster.getDst(), srcEdit, dstEdit)) {
        valid = srcCluster;
        break;
      }
    }
    return valid;
  }

  /**
   * Processes edit that is valid with a cluster.
   */
  private void processValid(final Edit srcEdit, final Edit dstEdit, Cluster valid) {
    final Cluster srcCluster = valid;
    final Cluster dstCluster = valid.getDst();
    srcCluster.getNodes().add(srcEdit);
    dstCluster.getNodes().add(dstEdit);
    final Tuple<RevisarTree<String>, RevisarTree<String>> au = AntiUnifierUtils.joinAntiUnify(
        srcEdit.getPlainTemplate(), dstEdit.getPlainTemplate(), 
        valid.getAu(), valid.getDst().getAu());
    final String srcAu = EquationUtils.convertToEq(au.getItem1());
    final String dstAu = EquationUtils.convertToEq(au.getItem2());
    srcCluster.setAu(srcAu);
    dstCluster.setAu(dstAu);
  }

  /**
   * Processes edit is not valid with any cluster.
   */
  private void processInvalid(final List<Cluster> src, final List<Cluster> dst, final Edit srcEdit,
      final Edit dstEdit) {
    final String srcAuCluster = srcEdit.getPlainTemplate();
    final String dstAuCluster = dstEdit.getPlainTemplate();
    final Cluster srcCluster = new Cluster(srcAuCluster, src.size() + srcClusters + "");
    final Cluster dstCluster = new Cluster(dstAuCluster, dst.size() + srcClusters + "");
    srcCluster.getNodes().add(srcEdit);
    dstCluster.getNodes().add(dstEdit);
    src.add(srcCluster);
    dst.add(dstCluster);
    srcCluster.setDst(dstCluster);
  }

  /**
   * Computes the source and target cluster given parameters.
   * 
   * @param srcEdits
   *          list of edits in the cluster
   * @param clusterId
   *          id of the cluster.
   * @return source and target cluster given parameters.
   */
  public Tuple<Cluster, Cluster> cluster(final List<Edit> srcEdits, final String clusterId) {
    String srcAu = srcEdits.get(0).getPlainTemplate();
    String dstAu = srcEdits.get(0).getDst().getPlainTemplate();
    for (int i = 1; i < srcEdits.size(); i++) {
      final String srcAuCluster = srcEdits.get(i).getPlainTemplate();
      final String dstAuCluster = srcEdits.get(i).getDst().getPlainTemplate();
      Tuple<RevisarTree<String>, RevisarTree<String>> tu = AntiUnifierUtils.joinAntiUnify(
          srcAuCluster, dstAuCluster, srcAu, dstAu);
      srcAu = EquationUtils.convertToEq(tu.getItem1());
      dstAu = EquationUtils.convertToEq(tu.getItem2());
      //AntiUnifier srcAu;
      //srcAu = AntiUnifierUtils.antiUnify(srcUnifier, srcAuCluster);
      //srcUnifier = EquationUtils.convertToEquation(srcAu);
      //final AntiUnifier dstAu = AntiUnifierUtils.antiUnify(dstUnifier, dstAuCluster);
      //dstUnifier = EquationUtils.convertToEquation(dstAu);
    }
    final Cluster srcCluster = new Cluster(srcAu, clusterId);
    final Cluster dstCluster = new Cluster(dstAu, clusterId);
    srcCluster.setDst(dstCluster);
    srcCluster.setNodes(srcEdits);
    final List<Edit> dstEdits = new ArrayList<>();
    for (final Edit edit : srcEdits) {
      final Edit dstEdit = edit.getDst();
      dstEdits.add(dstEdit);
    }
    dstCluster.setNodes(dstEdits);
    return new Tuple<>(srcCluster, dstCluster);
  }

  /**
   * Gets the best cluster.
   * 
   * @param srcCls
   *          cluster for source node
   * @param dstCls
   *          cluster for destination node
   * @param src
   *          src edit
   * @param dst
   *          dst edit
   * @return best cluster
   */
  public List<Tuple<Cluster, Double>> bestCluster(final List<Cluster> srcCls, 
      final List<Cluster> dstCls, final Edit src, final Edit dst) {
    final String srcAu = src.getPlainTemplate();
    final String dstAu = dst.getPlainTemplate();
    final List<Tuple<String, Double>> srcCost = CostUtils.cost(srcCls, srcAu);
    final List<Tuple<String, Double>> dstCost = CostUtils.cost(dstCls, dstAu);
    final List<Tuple<Cluster, Double>> totalCosts = new ArrayList<>();
    for (int i = 0; i < srcCost.size(); i++) {
      final Tuple<String, Double> srcTuple = srcCost.get(i);
      final Tuple<String, Double> dstTuple = dstCost.get(i);
      final double srcClusterCost = srcTuple.getItem2();
      final double dstClusterCost = dstTuple.getItem2();
      final double totalCost = srcClusterCost + dstClusterCost;
      final Tuple<Cluster, Double> tu = new Tuple<>(srcCls.get(i), totalCost);
      totalCosts.add(tu);
    }
    final Comparator<Tuple<Cluster, Double>> cmp = new Comparator<Tuple<Cluster, Double>>() {
      @Override
      public int compare(final Tuple<Cluster, Double> o1, final Tuple<Cluster, Double> o2) {
        return Double.compare(o1.getItem2(), o2.getItem2());
      }
    };
    totalCosts.sort(cmp);
    return totalCosts;
  }

  /**
   * Verifies whether a cluster is valid.
   */
  public boolean isValid(final Cluster srcCluster, final Cluster dstCluster, 
      final Edit src, final Edit dst) {
    try {
      final List<Edit> srcEdits = srcCluster.getNodes();
      final List<Edit> newSrcEdits = new ArrayList<>(srcEdits);
      newSrcEdits.add(src);
      return ClusterValidator.isValidTrans(newSrcEdits, srcCluster.getAu(), dstCluster.getAu());
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return false;
  }
}
