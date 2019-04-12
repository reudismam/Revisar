package br.ufcg.spg.tree;

import br.ufcg.spg.node.util.ASTNodeUtils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public class RevisarTreeUtils {
  
  private RevisarTreeUtils() {
  }
  
  /**
   * Gets root.
   * @param template template
   * @return root
   */
  public static String root(final String template) {
    final String value = template;
    final RevisarTree<String> tree = RevisarTreeParser.parser(value); 
    return tree.getValue().trim();
  }
  
  /**
   * Converts a ASTNode to a Revisar Tree.
   */
  public static RevisarTree<ASTNode> convertToRevisarTree(final ASTNode astNode) {
    final List<Object> children = ASTNodeUtils.getChildren(astNode);
    final List<ASTNode> normalizedChildren = ASTNodeUtils.normalize(children);
    RevisarTree<ASTNode> rtree = new RevisarTree<>(astNode, ASTNodeUtils.getLabel(astNode.getNodeType()));
    if (normalizedChildren.isEmpty()) {
      return rtree;
    }
    for (int i = 0; i < normalizedChildren.size(); i++) {
      final ASTNode sot = normalizedChildren.get(i);
      RevisarTree<ASTNode> child = convertToRevisarTree(sot);
      rtree.addChild(child);
    }
    return rtree;
  }
  
  /**
   * Get path from node to the root.
   */
  public static ArrayList<Integer> getPathToRoot(RevisarTree<ASTNode> node) {
    return getPathToRoot(node, new ArrayList<Integer>());
  }
  
  /**
   * Get path from node to the root.
   */
  private static ArrayList<Integer> getPathToRoot(RevisarTree<ASTNode> node, 
      ArrayList<Integer> currentPath) {
    if (node.getParent() == null) {
      return currentPath;
    }
    RevisarTree<ASTNode> parent = node.getParent();
    int posNode = parent.getChildren().indexOf(node);
    currentPath.add(0, posNode);
    return getPathToRoot(parent, currentPath);
  }
  
  /**
   * Get node given a path.
   */
  public static RevisarTree<ASTNode> getNodeFromPath(RevisarTree<ASTNode> node, 
      ArrayList<Integer> currentPath) {
    return getNodeFromPath(node, currentPath, 0);
  }
  
  /**
   * Get node given a path.
   */
  private static RevisarTree<ASTNode> getNodeFromPath(RevisarTree<ASTNode> node, 
      ArrayList<Integer> path, int currentIndex) {
    if (currentIndex == path.size()) {
      return node;
    }
    int childIndex = path.get(currentIndex);
    if (childIndex >= node.getChildren().size()) {
      return null;
    }
    RevisarTree<ASTNode> child = node.getChildren().get(childIndex);
    int newIndex = currentIndex + 1;
    return getNodeFromPath(child, path, newIndex);
  }
}
