package br.ufcg.spg.validator.node;

import br.ufcg.spg.node.util.ASTNodeUtils;
import br.ufcg.spg.tree.RevisarTreeUtils;

import org.eclipse.jdt.core.dom.ASTNode;

public class NodeValidator {
  /**
   * Verifies if node is valid.
   * @param template template
   * @return validity of the node
   */
  public static boolean isValidNode(String template) {
    String root = RevisarTreeUtils.root(template);
    if (root.equals(ASTNodeUtils.getLabel(ASTNode.IMPORT_DECLARATION))) {
      return false;
    }
    if (root.equals(ASTNodeUtils.getLabel(ASTNode.FIELD_DECLARATION))) {
      return false;
    }
    if (root.equals(ASTNodeUtils.getLabel(ASTNode.FIELD_ACCESS))) {
      return false;
    }
    if (root.equals(ASTNodeUtils.getLabel(ASTNode.TAG_ELEMENT))) {
      return false;
    }
    if (root.equals(ASTNodeUtils.getLabel(ASTNode.METHOD_DECLARATION))) {
      return false;
    }
    if (root.equals((ASTNodeUtils.getLabel(ASTNode.MEMBER_REF)))) {
      return false;
    }
    if (root.equals(ASTNodeUtils.getLabel(ASTNode.METHOD_REF_PARAMETER))) {
      return false;
    }
    if (root.equals(ASTNodeUtils.getLabel(ASTNode.METHOD_REF))) {
      return false;
    }
    return true;
  }
}
