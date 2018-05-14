package br.ufcg.spg.path;

import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;

import br.ufcg.spg.matcher.ASTNodeMatcher;
import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.RevisarTreeMatchCalculator;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeUtils;

public class PathUtils {
  
  public static String computePathRoot(ASTNode node) {
    String startPath = "";
    ASTNode root = node.getRoot();
    RevisarTree<ASTNode> tree = RevisarTreeUtils.convertToRevisarTree(root);
    IMatcher<RevisarTree<ASTNode>> matcher = new ASTNodeMatcher(node);
    MatchCalculator<RevisarTree<ASTNode>> calc = new RevisarTreeMatchCalculator<ASTNode>(matcher);
    RevisarTree<ASTNode> toSearch = calc.getNode(tree);
    return computePathRoot(toSearch, startPath);
  }
  
  private static String computePathRoot(RevisarTree<ASTNode> node, String initialPath) {
    if (node.getParent() == null) {
      return initialPath;
    }
    RevisarTree<ASTNode> parent = node.getParent();
    List<RevisarTree<ASTNode>> children = parent.getChildren();
    int index = children.indexOf(node);
    String newPath = index + "/"  + initialPath;
    return computePathRoot(parent, newPath);
  }
  
  public static ASTNode getNode(ASTNode root2, String path) {
    RevisarTree<ASTNode> root = RevisarTreeUtils.convertToRevisarTree(root2);
    if (path.isEmpty()) {
      return root.getValue();
    }
    String [] pathArray = path.split("/");
    RevisarTree<ASTNode> node = root;
    for (String current : pathArray) {
      if (node == null) {
        throw new RuntimeException("Unable to find node for this root node.");
      }
      List<RevisarTree<ASTNode>> children = node.getChildren();
      int index = Integer.parseInt(current);
      node = children.get(index);
    }
    return node.getValue();
  }
}
