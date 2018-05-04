package br.ufcg.spg.node;

import br.ufcg.spg.node.util.ASTNodeUtils;
import br.ufcg.spg.tree.RevisarTree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Class that extracts nodes from a given tree.
 */
public class NodesExtractor {
  /**
   * List of nodes.
   */
  private static List<ASTNode> nodes;
  
  /**
   * Gets the list of nodes.
   * 
   * @param node
   *          - node
   * @return node list
   */
  public static List<ASTNode> getNodes(final ASTNode node) {
    nodes = new ArrayList<ASTNode>();
    getNodeList(node);
    return nodes;
  }

  /**
   * Gets node given a root tree.
   * 
   * @param node
   *          root tree
   * @return list of nodes of this root tree.
   */
  public static <T> List<RevisarTree<T>> getNodes(final RevisarTree<T> node) {
    final List<RevisarTree<T>> nodesTree = new ArrayList<RevisarTree<T>>();
    getNodeList(node, nodesTree);
    return nodesTree;
  }

  /**
   * Gets the list of nodes.
   * 
   * @param node
   *          - node
   * @return node list
   */
  private static void getNodeList(final ASTNode node) {
    final List<Object> childrenNodes = ASTNodeUtils.getChildren(node);
    final List<ASTNode> children = ASTNodeUtils.normalize(childrenNodes);
    nodes.add(node);
    if (children.isEmpty()) {
      return;
    }
    nodes.add(node);
    for (int i = 0; i < children.size(); i++) {
      final ASTNode childNode = children.get(i);
      getNodeList(childNode);
    }
  }

  /**
   * Gets the list of nodes.
   * 
   * @param node
   *          - node
   * @return node list
   */
  private static <T> List<RevisarTree<T>> getNodeList(final RevisarTree<T> node,
      final List<RevisarTree<T>> nodesTree) {
    final List<RevisarTree<T>> children = node.getChildren();
    nodesTree.add(node);
    if (children.isEmpty()) {
      return nodesTree;
    }
    for (int i = 0; i < children.size(); i++) {
      final RevisarTree<T> childNode = children.get(i);
      getNodeList(childNode, nodesTree);
    }
    return nodesTree;
  }
}
