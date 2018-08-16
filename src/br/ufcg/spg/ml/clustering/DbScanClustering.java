package br.ufcg.spg.ml.clustering;

import at.unisalzburg.dbresearch.apted.costmodel.StringUnitCostModel;
import at.unisalzburg.dbresearch.apted.distance.APTED;
import at.unisalzburg.dbresearch.apted.node.Node;
import at.unisalzburg.dbresearch.apted.node.StringNodeData;
import at.unisalzburg.dbresearch.apted.parser.BracketStringInputParser;

import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.ml.editoperation.DeleteNode;
import br.ufcg.spg.ml.editoperation.IEditNode;
import br.ufcg.spg.ml.editoperation.InsertNode;
import br.ufcg.spg.ml.editoperation.Script;
import br.ufcg.spg.ml.editoperation.UpdateNode;
import br.ufcg.spg.ml.traversal.PostOrderTraversal;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeParser;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DbScanClustering {
  
  private DbScanClustering() {
  }
  
  private static final Logger logger = LogManager.getLogger(DbScanClustering.class.getName());

  /**
   * Gets the cluster.
   */
  public static Script getCluster(final Cluster srcCluster) {
    String srcAu = srcCluster.getAu();
    RevisarTree<String> srcTree = RevisarTreeParser.parser(srcAu);
    String srcEditTree = StringTreeConverter.convertRevisasrTreeToString(srcTree);
    Cluster dstCluster = srcCluster.getDst();
    String dstAu = dstCluster.getAu();
    RevisarTree<String> dstTree = RevisarTreeParser.parser(dstAu);
    String dstEditTree = StringTreeConverter.convertRevisasrTreeToString(dstTree);
    BracketStringInputParser parser = new BracketStringInputParser();
    Node<StringNodeData> t1 = parser.fromString(srcEditTree);
    Node<StringNodeData> t2 = parser.fromString(dstEditTree);
    APTED<StringUnitCostModel, StringNodeData> apted = new APTED<>(new StringUnitCostModel());
    apted.computeEditDistance(t1, t2);
    List<int[]> mapping = apted.computeEditMapping();
    PostOrderTraversal postOrder = new PostOrderTraversal();
    List<Node<StringNodeData>> left = postOrder.postOrderTraversal(t1);
    List<Node<StringNodeData>> right = postOrder.postOrderTraversal(t2);
    List<IEditNode> edits = edits(t1, t2, mapping, left, right);
    return new Script(edits, srcCluster);
  }

  /**
   * Computer edits.
   * @param t1 left tree.
   * @param t2 right tree.
   * @param mapping mapping between these two trees.
   * @param left nodes in first tree in a in-order traversal.
   * @param right nodes in the second tree in a in-order traversal.
   */
  private static List<IEditNode> edits(
      Node<StringNodeData> t1, Node<StringNodeData> t2, List<int[]> mapping,
      List<Node<StringNodeData>> left, List<Node<StringNodeData>> right) {
    List<IEditNode> edits = new ArrayList<>();
    for (final int[] map : mapping) {
      if (map[0] == 0) {
        Node<StringNodeData> parent = ClusteringTreeUtils.getParent(t2, right.get(map[1] - 1));
        String label = right.get(map[1] - 1).getNodeData().getLabel();
        label = configLabel(label);
        String parentLabel = parent != null ? parent.getNodeData().getLabel() : null;
        /*label = configLabel(label);
        if (!label.contains("hash")) {
          label = "node";
        }*/
        InsertNode insert = new InsertNode(parentLabel, label);
        logger.trace(insert);
        edits.add(insert);
      } else if (map[1] == 0) {
        Node<StringNodeData> parent = ClusteringTreeUtils.getParent(t1, left.get(map[0] - 1));
        String label = left.get(map[0] - 1).getNodeData().getLabel();
        label = configLabel(label);
        String parentLabel = parent != null ? parent.getNodeData().getLabel() : null;
        /*label = configLabel(label);
        if (!label.contains("hash")) {
          label = "node";
        }*/
        DeleteNode delete = new DeleteNode(parentLabel, label);
        edits.add(delete);
        logger.trace(delete);
      } else {
        String label1 = left.get(map[0] - 1).getNodeData().getLabel();
        String label2 = right.get(map[1] - 1).getNodeData().getLabel();
        if (!label1.equals(label2)) {
          label1 = configLabel(label1);
          if (!label1.contains("hash")) {
            label1 = "node";
          }
          label2 = configLabel(label2);
          if (!label2.contains("hash")) {
            label2 = "node";
          }   
          UpdateNode update = new UpdateNode(label1, label2);
          edits.add(update);
          logger.trace(update);
        } 
      }
    }
    return edits;
  }

  private static String configLabel(String label) {
    if (label.startsWith("hash_")) {
      label = "hash_";
    }
    return label;
  }
}