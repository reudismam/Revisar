package br.ufcg.spg.equation;

import br.ufcg.spg.analyzer.util.AnalyzerUtil;
import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.binding.BindingLocator;
import br.ufcg.spg.binding.BindingSolver;
import br.ufcg.spg.config.TechniqueConfig;
import br.ufcg.spg.mapper.AsciiMapper;
import br.ufcg.spg.tree.RevisarTree;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class EquationUtils {
    
  /**
   * Converts a ASTNode to an anti-unification equation.
   */
  public static String convertToEq(final RevisarTree<String> au) {
    if (au.getChildren().isEmpty()) {
      final String value = au.getValue();
      String content = value.trim();
      content = content.replaceAll("#", "hash_");
      content = content.replaceAll("\\$", "dollar_");
      if (content.startsWith("(")) {
        content = content.substring(1, content.length() - 1);
      }
      return content;
    }
    String tree = au.getValue() + "(";
    final RevisarTree<String> sotFirst = au.getChildren().get(0);
    final String nodeFirst = convertToEq(sotFirst);
    tree += nodeFirst;
    for (int i = 1; i < au.getChildren().size(); i++) {
      final RevisarTree<String> sot = au.getChildren().get(i);
      final String node = ", " + convertToEq(sot);
      tree += node;
    }
    tree += ")";
    return tree;
  }
  
  /**
   * Converts a ASTNode to an anti-unification equation.
   */
  public static String convertToAuEq(final ASTNode astNode) {
    return convertToEquation(astNode, TechniqueConfig.getInstance().isTemplateIncludesType());
  }

  /**
   * Converts a ASTNode to an anti-unification equation.
   */
  public static String convertToEquation(final AntiUnifier au) {
    if (au.getChildren().isEmpty()) {
      final String value = au.getValue().getUnifier();
      String content = value.trim();
      content = content.replaceAll("#", "hash_");
      content = content.replaceAll("\\$", "dollar_");
      if (content.startsWith("(")) {
        content = content.substring(1, content.length() - 1);
      }
      return content;
    }
    String tree = au.getValue() + "(";
    final AntiUnifier sotFirst = au.getChildren().get(0);
    final String nodeFirst = convertToEquation(sotFirst);
    tree += nodeFirst;
    for (int i = 1; i < au.getChildren().size(); i++) {
      final AntiUnifier sot = au.getChildren().get(i);
      final String node = ", " + convertToEquation(sot);
      tree += node;
    }
    tree += ")";
    return tree;
  }


  /**
   * Converts a ASTNode to an anti-unification equation.
   * 
   * @param list
   *          of nodes
   * @return ASTNode to an anti-unification equation.
   */
  public static String convertToEquation(final List<ASTNode> list) {
    if (list.isEmpty()) {
      throw new RuntimeException("List must contain at least one element.");
    }
    String tree = convertToAuEq(list.get(0));
    for (int i = 1; i < list.size(); i++) {
      tree += ", " + convertToAuEq(list.get(i));
    }
    return tree;
  }

  /**
   * Converts a ASTNode to an anti-unification equation.
   */
  public static String convertToEquation(final ASTNode astNode, final boolean includeType) {
    final List<Object> children = AnalyzerUtil.getChildren(astNode);
    final List<ASTNode> normalizedChildren = AnalyzerUtil.normalize(children);
    String type = qualifiedName(astNode);
    if (type == null) {
      type = "Unknown";
    }
    final String rootLabel = AnalyzerUtil.getLabel(astNode.getNodeType());
    final int nodeType = astNode.getNodeType();
    if (normalizedChildren.isEmpty()) {
      final String value = AnalyzerUtil.getValue(astNode);
      String content = value.trim();
      content = processStringLiteral(nodeType, content);
      content = processCharLiteral(nodeType, content);
      content = processTextElement(nodeType, content);
      content = processDimension(nodeType, content);
      content = processBreakStatement(content, nodeType); 
      content = processArrayInitializer(content, nodeType);
      content = processBlock(nodeType, content);
      content = processWildcard(nodeType, content);
      content = processEmpty(content);
      return processType(includeType, type, rootLabel, content);
    }
    String tree;
    if (includeType) {
      tree = rootLabel + "(type_" + type + ", ";
    } else {
      tree = rootLabel + "(";
    }
    if (nodeType == ASTNode.METHOD_INVOCATION) {
      final MethodInvocation minv = (MethodInvocation) astNode;
      final String name = minv.getName().getIdentifier();
      tree += "name_" + name + ", ";
    }
    final ASTNode sotFirst = normalizedChildren.get(0);
    final String nodeFirst = convertToAuEq(sotFirst);
    tree += nodeFirst;
    for (int i = 1; i < normalizedChildren.size(); i++) {
      final ASTNode sot = normalizedChildren.get(i);
      final String node = ", " + convertToAuEq(sot);
      tree += node;
    }
    tree += ")";
    return tree;
  }

  private static String processType(final boolean includeType, 
      final String type, final String rootLabel, final String content) {
    if (includeType) {
      final String treeNode = rootLabel + "(type_" + type + ", " + content + ")";
      return treeNode;
    } else {
      final String treeNode = rootLabel + "(" + content + ")";  
      return treeNode;
    }
  }

  private static String processEmpty(String content) {
    if (content.isEmpty()) {
      content = "empty";
    }
    return content;
  }

  private static String processWildcard(final int nodeType, String content) {
    if (nodeType == ASTNode.WILDCARD_TYPE) {
      content = content.replace("?", "Q_Mark");
    }
    return content;
  }

  private static String processBlock(final int nodeType, String content) {
    if (nodeType == ASTNode.BLOCK) {
      content = "" + AnalyzerUtil.getLabel(nodeType);
    }
    return content;
  }

  private static String processArrayInitializer(String content, final int nodeType) {
    if (nodeType == ASTNode.ARRAY_INITIALIZER) {
      content = AnalyzerUtil.getLabel(nodeType);
    }
    return content;
  }

  private static String processBreakStatement(String content, final int nodeType) {
    if (nodeType == ASTNode.BREAK_STATEMENT) {
      content = content.replaceAll("[^0-9a-zA-Z]+", "");
    }
    return content;
  }

  private static String processDimension(final int nodeType, String content) {
    if (nodeType == ASTNode.DIMENSION) {
      content = content.replaceAll("[^0-9a-zA-Z]+", "");
    }
    return content;
  }

  private static String processTextElement(final int nodeType, String content) {
    if (nodeType == ASTNode.TEXT_ELEMENT) {
      content = content.replaceAll("[^0-9a-zA-Z]+", "");
    }
    return content;
  }

  private static String processCharLiteral(final int nodeType, String content) {
    if (nodeType == ASTNode.CHARACTER_LITERAL) {
      content = extractChar(content);
    }
    return content;
  }

  private static String processStringLiteral(final int nodeType, String content) {
    if (nodeType == ASTNode.STRING_LITERAL) {
      // remove special character from the string
      final String temp = content.replaceAll("[^0-9a-zA-Z]+", "");
      if (!temp.isEmpty()) {
        content = temp;
      } else {
        content = extractChar(content);
      }
    }
    return content;
  }

  private static String qualifiedName(final ASTNode astNode) {
    String type = null;
    final String qualifiedName = BindingSolver.qualifiedName(astNode);
    if (qualifiedName != null) {
      type = qualifiedName.replace('<', '(').replace('>', ')');
      type = type.replace("?", "Q_Mark");
      type = type.replace("()", "_paren");
      type = type.replace("[]", "_array");
      try {
        final IJavaElement element = BindingSolver.typeBinding(astNode).getJavaElement();
        BindingLocator.resolveBinding(element);
      } catch (final CoreException e) {
        e.printStackTrace();
      }
    }
    return type;
  }
  
  private static String extractChar(String content) {
    if (!content.isEmpty()) {
      content = content.replaceAll("\"", "");
      content = content.replaceAll("\'", "");
      if (content.length() == 1) {
        content = "_" + AsciiMapper.descs(content.charAt(0));
      } else {
        content = "_STRING";
      }
    }
    return content;
  }  
}
