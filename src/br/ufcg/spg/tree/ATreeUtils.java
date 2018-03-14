package br.ufcg.spg.tree;

public class ATreeUtils {
  
  /**
   * Gets root.
   * @param template template
   * @return root
   */
  public static String root(final String template) {
    final String value = template;
    final ATree<String> tree = AParser.parser(value); 
    final String root = tree.getValue().trim();    
    return root;
  }
}
