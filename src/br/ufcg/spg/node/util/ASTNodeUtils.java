package br.ufcg.spg.node.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.internal.core.dom.rewrite.ASTRewriteFlattener;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore;

/**
 * Utility class.
 */
public final class ASTNodeUtils {
  
  private ASTNodeUtils() {
    throw new RuntimeException("Utility class cannot be instantiated.");
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
   * Gets the label for an integer kind.
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
            throw new RuntimeException(ex);
          }
        }
      } 
    }
    return Integer.toString(label);
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
