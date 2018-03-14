package br.ufcg.spg.analyzer.util;

import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.tree.RevisarTree;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.internal.core.dom.rewrite.ASTRewriteFlattener;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore;

/**
 * Analyzer utils.
 */
public final class AnalyzerUtil {

  /**
   * List of nodes.
   */
  private static List<ASTNode> nodes;

  /**
   * Constructor.
   */
  private AnalyzerUtil() {
    super();
  }

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
    final List<Object> childrenNodes = AnalyzerUtil.getChildren(node);
    final List<ASTNode> children = AnalyzerUtil.normalize(childrenNodes);
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

  /**
   * Get children of an ASTNode.
   */
  public static List<Object> getChildren(final ASTNode node) {
    final List<Object> children = new ArrayList<Object>();
    final List<?> list = node.structuralPropertiesForType();
    for (int i = 0; i < list.size(); i++) {
      final Object child = node.getStructuralProperty((StructuralPropertyDescriptor) list.get(i));
      if (child instanceof ASTNode) {
        children.add(child);
      } else if (child instanceof List) {
        final List<?> clist = (List<?>) child;
        if (!clist.isEmpty()) {
          children.add(clist);
        }
      }
    }
    return children;
  }

  /**
   * Gets value of an AST node.
   * 
   * @param node
   *          node
   * @return string value
   */
  public static String getValue(final ASTNode node) {
    return ASTRewriteFlattener.asString(node, new RewriteEventStore());
  }

  /**
   * Gets the label for an int kind.
   * 
   */
  public static String getLabel(final int label) {
    final Field[] declaredFields = ASTNode.class.getDeclaredFields();
    for (final Field field : declaredFields) {
      if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
        if (field.getType() == int.class) {
          try {
            if (field.getInt(null) == label) {
              return field.getName();
            }
          } catch (final IllegalAccessException ex) {
            // empty
          }
        }
      } 
    }
    return Integer.toString(label);
  }

  /**
   * Get the root of an anti-unification algorithm.
   * 
   * @param au
   *          - anti-unification
   * @return root of the anti-unification
   */
  public static AntiUnifier getRoot(final AntiUnifier au) {
    AntiUnifier root = au.getParent();
    AntiUnifier previous = au;
    while (root != null) {
      previous = root;
      root = root.getParent();
    }
    return previous;
  }

  /**
   * Normalizes children nodes.
   * 
   * @param children
   *          children to be analyzed.
   * @return normalized children.
   */
  public static List<ASTNode> normalize(final List<Object> children) {
    final List<ASTNode> nodes = new ArrayList<ASTNode>();
    for (final Object obj : children) {
      if (obj instanceof ASTNode) {
        final ASTNode node = (ASTNode) obj;
        nodes.add(node);
      } else {
        @SuppressWarnings("unchecked")
        final List<ASTNode> astNodes = (List<ASTNode>) obj;
        nodes.addAll(astNodes);
      }
    }
    return nodes;
  }
}
