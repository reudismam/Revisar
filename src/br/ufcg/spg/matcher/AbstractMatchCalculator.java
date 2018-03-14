package br.ufcg.spg.matcher;

import br.ufcg.spg.analyzer.util.AnalyzerUtil;
import com.github.gumtreediff.tree.ITree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Performs match operations.
 *
 */
public abstract class AbstractMatchCalculator {

  /**
   * List of nodes in the AST.
   */
  private transient List<ASTNode> nodes;

  /**
   * List of indexes for each node.
   */
  private transient List<Integer> indexes;

  /**
   * List of trees.
   */
  private transient List<ITree> trees;

  /**
   * Current index.
   */
  protected transient int currentIndex;

  /**
   * Constructor.
   */
  public AbstractMatchCalculator() {
    super();
  }

  /**
   * Returns nodes for a kind
   * 
   * @param node
   *          - node
   * @return node that starts and ends at position.
   */
  private void findNode(final ITree node) {
    currentIndex = 0;
    trees = new ArrayList<ITree>();
    indexes = new ArrayList<Integer>();
    buildNodesFromRoot(node);
  }

  /**
   * Returns node at given start and position.
   * 
   * @param root
   *          root node
   */
  public void fillNode(final ASTNode root) {
    currentIndex = 0;
    nodes = new ArrayList<ASTNode>();
    indexes = new ArrayList<Integer>();
    buildNodesFromRoot(root);
  }

  /**
   * Gets node.
   * 
   * @param root
   *          root node
   * @return node in root
   */
  public ITree getNode(final ITree root) {
    //if (trees == null) {
    findNode(root);
    //}
    if (trees.isEmpty()) {
      return null;
    }
    return trees.get(0);
  }

  /**
   * Gets node.
   * 
   * @param root
   *          root node
   * @return node in root
   */
  public ASTNode getNode(final ASTNode root) {
    //if (nodes == null) {
    fillNode(root);
    //}
    if (nodes.isEmpty()) {
      return null;
    }
    return nodes.get(0);
  }
  
  /**
   * Gets node.
   * 
   * @param root
   *          root node
   * @return node in root
   */
  public List<ASTNode> getNodes(final ASTNode root) {
    //if (nodes == null) {
    fillNode(root);
    //}
    return nodes;
  }

  /**
   * Gets index.
   * 
   * @param root
   *          root node
   * @return index
   */
  public int getIndex(final ITree root) {
    //if (indexes == null) {
    findNode(root);
    //}
    if (indexes.isEmpty()) {
      return -1;
    }
    return indexes.get(0);
  }

  /**
   * Gets index.
   * 
   * @param root
   *          root node
   * @return index
   */
  public int getIndex(final ASTNode root) {
    //if (indexes == null) {
    fillNode(root);
    //}
    if (indexes.isEmpty()) {
      return -1;
    }
    return indexes.get(0);
  }

  /**
   * Returns nodes for a position.
   * 
   * @param obj
   *          current object
   * @param start
   *          time
   * @param end
   *          time
   */
  private void buildNodesFromRoot(final ITree st) {
    currentIndex++;
    if (evaluate(st)) {
      trees.add(st);
      indexes.add(currentIndex);
    }
    final List<ITree> children = st.getChildren();
    for (int i = 0; i < children.size(); i++) {
      final ITree child = children.get(i);
      buildNodesFromRoot(child);
    }
    return;
  }

  /**
   * Returns nodes for a position.
   * 
   * @param obj
   *          current object
   * @param start
   *          time
   * @param end
   *          time
   */
  private void buildNodesFromRoot(final ASTNode obj) {
    currentIndex++;
    final ASTNode st = obj;
    if (evaluate(st)) {
      nodes.add(st);
      indexes.add(currentIndex);
    }
    final List<Object> childrenObjects = AnalyzerUtil.getChildren(st);
    final List<ASTNode> children = AnalyzerUtil.normalize(childrenObjects);
    for (int i = 0; i < children.size(); i++) {
      final ASTNode sot = children.get(i);
      buildNodesFromRoot(sot);
    }
  }

  protected abstract boolean evaluate(ITree st);

  protected abstract boolean evaluate(ASTNode st);
}
