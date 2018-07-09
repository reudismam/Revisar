package br.ufcg.spg.ml.clustering;

import at.unisalzburg.dbresearch.apted.node.Node;
import at.unisalzburg.dbresearch.apted.node.StringNodeData;

/**
 * Utility class for clustering in dbscan.
 */
public class ClusteringTreeUtils {
  
  private ClusteringTreeUtils() {
  }

  /**
   * Get parent of a node.
   */
  public static Node<StringNodeData> getParent(
      Node<StringNodeData> root, Node<StringNodeData> target) {
    if (target.equals(root)) {
      return null;
    }
    return ClusteringTreeUtils.getParentForNode(root, target);
  }

  private static Node<StringNodeData> getParentForNode(
      Node<StringNodeData> parent, Node<StringNodeData> target) {
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
        } else {
          return returnValue;
        }
      }
    }
    return null;
  }
}
