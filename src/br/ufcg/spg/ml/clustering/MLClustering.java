package br.ufcg.spg.ml.clustering;

import java.util.ArrayList;
import java.util.List;

import org.christopherfrantz.dbscan.DBSCANClusterer;
import org.christopherfrantz.dbscan.DBSCANClusteringException;

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
import br.ufcg.spg.ml.traversal.ScriptDistanceMetric;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeParser;

public class MLClustering {

  public static String convertRevisasrTreeToString(RevisarTree<String> st) {
    List<RevisarTree<String>> list = st.getChildren();
    if (list.isEmpty()) {
      String content = st.getValue();
      String treeNode = "{" + content + "}";
      return treeNode;
    }
    String tree = "{" + st.getValue();
    for (RevisarTree<String> sot : st.getChildren()) {
      String node = convertRevisasrTreeToString(sot);
      tree += node;
    }
    tree += "}";
    return tree;
  }

  public static Script getCluster(final Cluster srcCluster) {
    String srcAu = srcCluster.getAu();
    RevisarTree<String> srcTree = RevisarTreeParser.parser(srcAu);
    String srcEditTree = convertRevisasrTreeToString(srcTree);
    
    Cluster dstCluster = srcCluster.getDst();
    String dstAu = dstCluster.getAu();
    RevisarTree<String> dstTree = RevisarTreeParser.parser(dstAu);
    String dstEditTree = convertRevisasrTreeToString(dstTree);
    
    BracketStringInputParser parser = new BracketStringInputParser();
    Node<StringNodeData> t1 = parser.fromString(srcEditTree);
    Node<StringNodeData> t2 = parser.fromString(dstEditTree);
    // Initialise APTED.
    APTED<StringUnitCostModel, StringNodeData> apted = new APTED<>(new StringUnitCostModel());
    // Execute APTED.
    apted.computeEditDistance(t1, t2);
    List<int[]> mapping = apted.computeEditMapping();
    
    PostOrderTraversal postOrder = new PostOrderTraversal();
    
    List<Node<StringNodeData>> left = postOrder.postOrderTraversal(t1);
    List<Node<StringNodeData>> right = postOrder.postOrderTraversal(t2);
    
    List<IEditNode> edits = new ArrayList<>();
    for (final int[] map : mapping) {
      if (map[0] == 0) {
        Node<StringNodeData> parent = getParent(t2, right.get(map[1] - 1));
        String label = right.get(map[1] - 1).getNodeData().getLabel();
        if (label.startsWith("hash_")) {
          label = "hash_";
        }
        String parentLabel = parent != null ? parent.getNodeData().getLabel() : null;
        InsertNode insert = new InsertNode(parentLabel, label);
        System.out.println(insert);
        edits.add(insert);
      } else if (map[1] == 0) {
        Node<StringNodeData> parent = getParent(t1, left.get(map[0] - 1));
        String label = left.get(map[0] - 1).getNodeData().getLabel();
        if (label.startsWith("hash_")) {
          label = "hash_";
        }
        String parentLabel = parent != null ? parent.getNodeData().getLabel() : null;
        DeleteNode delete = new DeleteNode(parentLabel, label);
        edits.add(delete);
        System.out.println(delete);
      } else {      
        String label1 = left.get(map[0] - 1).getNodeData().getLabel().toString();
        String label2 = right.get(map[1] - 1).getNodeData().getLabel().toString();
        if (label1.startsWith("hash_")) {
          label1 = "hash_";
        }
//        else if (left.get(map[0] - 1).getChildren().isEmpty()) {
//          label1 = "value";
//        }
        if (label2.startsWith("hash_")) {
          label2 = "hash_";
        }
//        else if (right.get(map[1] - 1).getChildren().isEmpty()) {
//          label2 = "value";
//        }
        if (!label1.equals(label2)) {
          UpdateNode update = new UpdateNode(label1, label2);
          edits.add(update);
          System.out.println(update);
        }
      }
    }
    Script script = new Script(edits, srcCluster);
    return script;
  }
  
  public static Node<StringNodeData> getParent(Node<StringNodeData> root, Node<StringNodeData> target) {
    if (target.equals(root)) {
      return null;
    }
    return getParentForNode(root, target);
  }
  
  private static Node<StringNodeData> getParentForNode(Node<StringNodeData> parent, Node<StringNodeData> target) {
    if (parent.equals(target)) {
      return target;
    }
    if (parent.getChildren().isEmpty()) {
      return null;
    }
    for (Node<StringNodeData> node : parent.getChildren()) {
      Node<StringNodeData> returnValue = getParentForNode(node, target);
      if (returnValue != null) {
        if (returnValue.equals(target)) {
          return parent;
        } 
        else {
          return returnValue;
        }
      }
    }
    return null;
  }
  
  
  public static List<ArrayList<Script>> cluster(List<Script> scripts) {
    try {
      DBSCANClusterer<Script> dbscan = new DBSCANClusterer<Script>(scripts, 2, 0.5, new ScriptDistanceMetric());
      List<ArrayList<Script>> clusters = dbscan.performClustering();
      return clusters;
    } catch (DBSCANClusteringException e) {
      e.printStackTrace();
      return null;
    }
  }
}