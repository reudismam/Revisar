package br.ufcg.spg.ml.clustering;

import at.unisalzburg.dbresearch.apted.costmodel.StringUnitCostModel;
import at.unisalzburg.dbresearch.apted.distance.APTED;
import at.unisalzburg.dbresearch.apted.node.Node;
import at.unisalzburg.dbresearch.apted.node.StringNodeData;
import at.unisalzburg.dbresearch.apted.parser.BracketStringInputParser;

import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.component.ConnectedComponentManager;
import br.ufcg.spg.component.FullConnectedEditNode;
import br.ufcg.spg.converter.ConverterHelper;
import br.ufcg.spg.edit.EditScriptGenerator;
import br.ufcg.spg.ml.editoperation.EditNode;
import br.ufcg.spg.ml.editoperation.Script;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeParser;
import br.ufcg.spg.tree.TreeTraversal;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public final class EditScriptUtils {

  private EditScriptUtils() {
  }

  /**
   * Gets the cluster.
   */
  public static Script<StringNodeData> getCluster(final Cluster srcCluster) {
    String srcAu = srcCluster.getAu();
    srcAu = "ROOT(" + srcAu + ")";
    RevisarTree<String> srcTree = RevisarTreeParser.parser(srcAu);
    String srcEditTree = StringTreeConverter.convertRevisasrTreeToString(srcTree);
    Cluster dstCluster = srcCluster.getDst();
    String dstAu = dstCluster.getAu();
    dstAu = "ROOT(" + dstAu + ")";
    RevisarTree<String> dstTree = RevisarTreeParser.parser(dstAu);
    String dstEditTree = StringTreeConverter.convertRevisasrTreeToString(dstTree);
    BracketStringInputParser parser = new BracketStringInputParser();
    Node<StringNodeData> t1 = parser.fromString(srcEditTree);
    Node<StringNodeData> t2 = parser.fromString(dstEditTree);
    APTED<StringUnitCostModel, StringNodeData> apted = new APTED<>(new StringUnitCostModel());
    apted.computeEditDistance(t1, t2);
    List<int[]> mapping = apted.computeEditMapping();
    // PostOrderTraversal postOrder = new PostOrderTraversal();
    // List<Node<StringNodeData>> left = postOrder.postOrderTraversal(t1);
    // List<Node<StringNodeData>> right = postOrder.postOrderTraversal(t2);
    RevisarTree<StringNodeData> rt1 = ConverterHelper.convertNodeToRevisarTree(t1);
    RevisarTree<StringNodeData> rt2 = ConverterHelper.convertNodeToRevisarTree(t2);
    List<RevisarTree<StringNodeData>> left = 
        new TreeTraversal<StringNodeData>().postOrderTraversal(rt1);
    List<RevisarTree<StringNodeData>> right = 
        new TreeTraversal<StringNodeData>().postOrderTraversal(rt2);
    Map<RevisarTree<StringNodeData>, RevisarTree<StringNodeData>> m 
        = mapping(t1, t2, mapping, left, right);
    List<EditNode<StringNodeData>> edits = 
        new EditScriptGenerator<StringNodeData>().editScript(rt1, rt2, m);
    Script<StringNodeData> script = new Script<StringNodeData>(edits, srcCluster);
    ConnectedComponentManager<EditNode<StringNodeData>> con = new ConnectedComponentManager<>();
    List<List<EditNode<StringNodeData>>> c = con.connectedComponents(script.getList(), 
        new FullConnectedEditNode<StringNodeData>(script.getList()));
    return script;
  }

  /**
   * Computer edits.
   * 
   * @param t1
   *          left tree.
   * @param t2
   *          right tree.
   * @param mapping
   *          mapping between these two trees.
   * @param left
   *          nodes in first tree in a in-order traversal.
   * @param right
   *          nodes in the second tree in a in-order traversal.
   */
  private static Map<RevisarTree<StringNodeData>, RevisarTree<StringNodeData>> mapping(
      Node<StringNodeData> t1, Node<StringNodeData> t2, 
      List<int[]> mapping, List<RevisarTree<StringNodeData>> left,
      List<RevisarTree<StringNodeData>> right) {
    Map<RevisarTree<StringNodeData>, RevisarTree<StringNodeData>> m = 
        new Hashtable<>();
    for (final int[] map : mapping) {
      if (map[0] != 0 && map[1] != 0) {
        RevisarTree<StringNodeData> nodeT1 = left.get(map[0] - 1);
        RevisarTree<StringNodeData> nodeT2 = right.get(map[1] - 1);
        m.put(nodeT1, nodeT2);
      }
    }
    return m;
  }
  
// /**
//  * Computer edits.
//  * @param t1 left tree.
//  * @param t2 right tree.
//  * @param mapping mapping between these two trees.
//  * @param left nodes in first tree in a in-order traversal.
//  * @param right nodes in the second tree in a in-order traversal.
//  */
//  private static List<EditNode> edits(
//  Node<StringNodeData> t1, Node<StringNodeData> t2, List<int[]> mapping,
//  List<Node<StringNodeData>> left, List<Node<StringNodeData>> right) {
//  List<EditNode> edits = new ArrayList<>();
//  for (final int[] map : mapping) {
//  if (map[0] == 0) {
//  Node<StringNodeData> parent = ClusteringTreeUtils.getParent(t2,
//  right.get(map[1] - 1));
//  String label = right.get(map[1] - 1).getNodeData().getLabel();
//  label = configLabel(label);
//  String parentLabel = parent != null ? parent.getNodeData().getLabel() :
 // null;
 // /*label = configLabel(label);
 // if (!label.contains("hash")) {
 // label = "node";
 // }*/
 // InsertNode insert = new InsertNode(parentLabel, label);
 // logger.trace(insert);
 // edits.add(insert);
 // } else if (map[1] == 0) {
 // Node<StringNodeData> parent = ClusteringTreeUtils.getParent(t1,
 // left.get(map[0] - 1));
 // String label = left.get(map[0] - 1).getNodeData().getLabel();
 // label = configLabel(label);
 // String parentLabel = parent != null ? parent.getNodeData().getLabel() :
 // null;
 // /*label = configLabel(label);
 // if (!label.contains("hash")) {
 // label = "node";
 // }*/
 // DeleteNode delete = new DeleteNode(parentLabel, label);
 // edits.add(delete);
 // logger.trace(delete);
 // } else {
 // String label1 = left.get(map[0] - 1).getNodeData().getLabel();
 // String label2 = right.get(map[1] - 1).getNodeData().getLabel();
 // if (!label1.equals(label2)) {
 // label1 = configLabel(label1);
 // if (!label1.contains("hash")) {
 // label1 = "node";
 // }
 // label2 = configLabel(label2);
 // if (!label2.contains("hash")) {
 // label2 = "node";
 // }
 // UpdateNode update = new UpdateNode(label1, label2);
 // edits.add(update);
 // logger.trace(update);
 // }
 // }
 // }
 // return edits;
 // }


  // /**
  // * Computer edits.
  // * @param t1 left tree.
  // * @param t2 right tree.
  // * @param mapping mapping between these two trees.
  // * @param left nodes in first tree in a in-order traversal.
  // * @param right nodes in the second tree in a in-order traversal.
  // */
  // private static List<EditNode> edits(
  // Node<StringNodeData> t1, Node<StringNodeData> t2, List<int[]> mapping,
  // List<Node<StringNodeData>> left, List<Node<StringNodeData>> right) {
  // List<EditNode> edits = new ArrayList<>();
  // for (final int[] map : mapping) {
  // if (map[0] == 0) {
  // Node<StringNodeData> parent = ClusteringTreeUtils.getParent(t2,
  // right.get(map[1] - 1));
  // String label = right.get(map[1] - 1).getNodeData().getLabel();
  // label = configLabel(label);
  // String parentLabel = parent != null ? parent.getNodeData().getLabel() :
  // null;
  // /*label = configLabel(label);
  // if (!label.contains("hash")) {
  // label = "node";
  // }*/
  // InsertNode insert = new InsertNode(parentLabel, label);
  // logger.trace(insert);
  // edits.add(insert);
  // } else if (map[1] == 0) {
  // Node<StringNodeData> parent = ClusteringTreeUtils.getParent(t1,
  // left.get(map[0] - 1));
  // String label = left.get(map[0] - 1).getNodeData().getLabel();
  // label = configLabel(label);
  // String parentLabel = parent != null ? parent.getNodeData().getLabel() :
  // null;
  // /*label = configLabel(label);
  // if (!label.contains("hash")) {
  // label = "node";
  // }*/
  // DeleteNode delete = new DeleteNode(parentLabel, label);
  // edits.add(delete);
  // logger.trace(delete);
  // } else {
  // String label1 = left.get(map[0] - 1).getNodeData().getLabel();
  // String label2 = right.get(map[1] - 1).getNodeData().getLabel();
  // if (!label1.equals(label2)) {
  // label1 = configLabel(label1);
  // if (!label1.contains("hash")) {
  // label1 = "node";
  // }
  // label2 = configLabel(label2);
  // if (!label2.contains("hash")) {
  // label2 = "node";
  // }
  // UpdateNode update = new UpdateNode(label1, label2);
  // edits.add(update);
  // logger.trace(update);
  // }
  // }
  // }
  // return edits;
  // }

  /**
   * @param label
   * @return
   */
  private static String configLabel(String label) {
    if (label.startsWith("hash_")) {
      label = "hash_";
    }
    return label;
  }
}