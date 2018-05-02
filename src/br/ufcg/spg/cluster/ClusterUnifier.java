package br.ufcg.spg.cluster;

import at.jku.risc.stout.urauc.algo.JustificationException;
import at.jku.risc.stout.urauc.util.ControlledException;
import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.antiunification.AntiUnifierUtils;
import br.ufcg.spg.antiunification.dist.AntiUnifierDistanceUtils;
import br.ufcg.spg.antiunification.dist.LeftDistanceCalculator;
import br.ufcg.spg.antiunification.dist.RightDistanceCalculator;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.config.TechniqueConfig;
import br.ufcg.spg.database.ClusterDao;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.edit.EditStorage;
import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.transformation.TransformationUtils;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.util.PrintUtils;
import br.ufcg.spg.validator.ClusterValidator;
import br.ufcg.spg.validator.node.NodeValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Unifier cluster class.
 */
public final class ClusterUnifier {
  /**
   * Number of clusters processed.
   */
  private transient int srcClusters;

  /**
   * Singleton instance.
   */
  private static ClusterUnifier instance;

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
      System.out.println(((double) d) / allDcaps.size() + " % completed.");
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
   * @param srcList source clusters
   * @return clusters
   */
  public List<Cluster> clusters(final List<Edit> srcList) throws IOException {
    final List<Cluster> src = new ArrayList<>();
    final TechniqueConfig config = TechniqueConfig.getInstance();
    final Map<String, List<Edit>> groups = groupEditsByDCap(srcList, config);
    for (final Entry<String, List<Edit>> entry: groups.entrySet()) {
      final List<Edit> toAnalyze = entry.getValue();
      final List<Cluster> clts =  clusterEdits(toAnalyze);
      src.addAll(clts);
    }
    return src;
  }

  /**
   * Groups edits by d-cap.
   */
  private Map<String, List<Edit>> groupEditsByDCap(final List<Edit> srcList, 
      final TechniqueConfig config) {
    final Map<String, List<Edit>> groups = new Hashtable<>();
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
  public List<Cluster> clusterEdits(final List<Edit> srcList) throws IOException {
    final List<Cluster> src = new ArrayList<Cluster>();
    final List<Cluster> dst = new ArrayList<Cluster>();
    System.out.println("CLUSTER LIST");
    if (srcList.isEmpty()) {
      return src;
    }
    for (int i = 0; i < srcList.size(); i++) {
      System.out.println(((double) i) / srcList.size() + "% complete for d-cap.");
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
          processInvalid(src, dst, srcEdit, dstEdit);
        } 
      } catch (final Exception e) {
        e.printStackTrace();
      }
      System.out.println("DEBUG: " + (src.size() + srcClusters - 1));
    }
    return src;
  }

  /**
   * Searches for a valid cluster to receive the edits.
   */
  private Cluster searchForValid(final Edit srcEdit, final Edit dstEdit, 
      final List<Tuple<Cluster, Double>> costs) {
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
    final AntiUnifier srcUni = 
        antiUnify(srcCluster.getAu(), srcEdit.getPlainTemplate());
    final String srcAu = EquationUtils.convertToEquation(srcUni);
    final AntiUnifier dstUni = 
        antiUnify(dstCluster.getAu(), dstEdit.getPlainTemplate());
    final String dstAu = EquationUtils.convertToEquation(dstUni);
    srcCluster.setAu(srcAu);
    dstCluster.setAu(dstAu);
  }

  /**
   * Processes edit is not valid with any cluster.
   */
  private void processInvalid(final List<Cluster> src, 
      final List<Cluster> dst, final Edit srcEdit, final Edit dstEdit) {
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
  public Tuple<Cluster, Cluster> cluster(final List<Edit> srcEdits, final String clusterId)
      throws JustificationException, IOException, ControlledException {
    String srcUnifier = srcEdits.get(0).getPlainTemplate();
    String dstUnifier = srcEdits.get(0).getDst().getPlainTemplate();
    for (int i = 1; i < srcEdits.size(); i++) {
      final String srcAuCluster = srcEdits.get(i).getPlainTemplate();
      final String dstAuCluster = srcEdits.get(i).getDst().getPlainTemplate();
      final AntiUnifier srcAu = antiUnify(srcUnifier, srcAuCluster);
      srcUnifier = EquationUtils.convertToEquation(srcAu);
      final AntiUnifier dstAu = antiUnify(dstUnifier, dstAuCluster);
      dstUnifier = EquationUtils.convertToEquation(dstAu);
    }
    final Cluster srcCluster = new Cluster(srcUnifier, clusterId);
    final Cluster dstCluster = new Cluster(dstUnifier, clusterId);
    srcCluster.setNodes(srcEdits);
    final List<Edit> dstEdits = new ArrayList<Edit>();
    for (final Edit edit : srcEdits) {
      final Edit dstEdit = edit.getDst();
      dstEdits.add(dstEdit);
    }
    dstCluster.setNodes(dstEdits);
    final Tuple<Cluster, Cluster> tuCluster = new Tuple<>(srcCluster, dstCluster);
    return tuCluster;
  }

  /**
   * Gets the best cluster.
   * 
   * @param srcCls
   *          cluster for source node
   * @param dstCls
   *          cluster for destination node
   * @param srcAu
   *          source code unification
   * @param dstAu
   *          destination code unification
   * @return best cluster
   */
  private List<Tuple<Cluster, Double>> bestCluster(final List<Cluster> srcCls, 
      final List<Cluster> dstCls, 
      final Edit src, final Edit dst) {
    final String srcAu = src.getPlainTemplate();
    final String dstAu = dst.getPlainTemplate();
    final List<Tuple<String, Double>> srcCost = cost(srcCls, srcAu);
    final List<Tuple<String, Double>> dstCost = cost(dstCls, dstAu);
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

  private boolean isValid(final Cluster srcCluster, final Cluster dstCluster, 
      final Edit src, final Edit dst) {
    final List<Edit> srcEdits = srcCluster.getNodes();
    final AntiUnifier srcAu = antiUnify(srcCluster.getAu(), src.getPlainTemplate());
    final String srcUnifier = EquationUtils.convertToEquation(srcAu);
    final AntiUnifier dstAu = antiUnify(dstCluster.getAu(), dst.getPlainTemplate());
    final String dstUnifier = EquationUtils.convertToEquation(dstAu);
    try {
      final List<Edit> newSrcEdits = new ArrayList<>(srcEdits);
      newSrcEdits.add(src);
      final boolean valid = ClusterValidator.isValidTrans(newSrcEdits, srcUnifier, dstUnifier);
      return valid;
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return false;
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
  private static List<Tuple<String, Double>> cost(final List<Cluster> clusters, final String au) {
    final List<Tuple<String, Double>> cost = new ArrayList<>();
    for (final Cluster cluster : clusters) {
      final AntiUnifier unifier = antiUnify(cluster.getAu(), au);
      final String unifierStr = EquationUtils.convertToEquation(unifier);
      final double clusterCost = computerAddCost(unifier);
      final Tuple<String, Double> tuple = new Tuple<>(unifierStr, clusterCost);
      cost.add(tuple);
    }
    return cost;
  }

  /**
   * Computes anti-unification.
   * 
   * @param au
   *          first anti-unification
   * @param clusterAu
   *          second anti-unification
   * @return anti-unification
   */
  public static AntiUnifier computeUnification(final AntiUnifier au, final AntiUnifier clusterAu) {
    final String eqCluster = EquationUtils.convertToEquation(clusterAu);
    final String eqOther = EquationUtils.convertToEquation(au);
    return antiUnify(eqCluster, eqOther);
  }

  /**
   * Computes anti-unification.
   * @param au
   *          second anti-unification
   * @param clusterAu
   *          first anti-unification
   * 
   * @return anti-unification
   */
  public static AntiUnifier antiUnify(final String au, final String clusterAu) {
    AntiUnifier unifier;
    final String eqCluster = au;
    final String eqOther = clusterAu;
    try {
      unifier = AntiUnifierUtils.antiUnify(eqCluster, eqOther);
      return unifier;
    } catch (ControlledException | IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Defines the add cost function.
   * 
   * @param root
   *          node
   * @return cost of unifying two trees
   */
  private static double computerAddCost(final AntiUnifier root) {
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
    final double cost = ((double) (leftSubstitutions + rightSubstitutions) - placeholders) / size;
    return cost;
  }
}
