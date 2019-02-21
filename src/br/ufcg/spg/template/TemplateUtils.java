package br.ufcg.spg.template;

import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeParser;

import java.util.ArrayList;
import java.util.List;

public class TemplateUtils {
  
  private TemplateUtils() {
  }

  /**
   * Removes information.
   * 
   * @param au
   *          ant-unification
   * @param toRemove nodes to be removed.
   * @return the type information
   */
  private static RevisarTree<String> remove(final RevisarTree<String> au, 
      final List<String> toRemove) {
    if (au.getChildren().isEmpty()) {
      if (!containsAny(au.getValue(), toRemove)) {
        return au;
      }
      return null;
    }
    final RevisarTree<String> newAu = new RevisarTree<>(au.getValue(), "");
    for (final RevisarTree<String> aui : au.getChildren()) {
      final RevisarTree<String> rau = remove(aui, toRemove);
      if (rau != null) {
        newAu.addChild(rau);
      }
    }
    return newAu;
  }
  
  /**
   * Removes the type information.
   * 
   * @param au
   *          ant-unification
   * @return the type information
   */
  public static RevisarTree<String> removeType(final RevisarTree<String> au) {
    final List<String> list = new ArrayList<>();
    list.add("type_");
    return remove(au, list);
  }
  
  /**
   * Remove type.
   * @param template template
   */
  public static String removeType(final String template) {
    final RevisarTree<String> atree = RevisarTreeParser.parser(template);
    final RevisarTree<String> atreeplain = TemplateUtils.removeType(atree);
    return EquationUtils.convertToEq(atreeplain);
  }
  
  /**
   * Removes the type information.
   * 
   * @param au
   *          ant-unification
   * @return the type information
   */
  public static RevisarTree<String> removeName(final RevisarTree<String> au) {
    final List<String> list = new ArrayList<>();
    list.add("name_");
    return remove(au, list);
  }
  
  /**
   * Remove name.
   * @param template template
   */
  public static String removeName(final String template) {
    final RevisarTree<String> atree = RevisarTreeParser.parser(template);
    final RevisarTree<String> atreeplain = TemplateUtils.removeName(atree);
    return EquationUtils.convertToEq(atreeplain);
  }
  
  /**
   * Removes the type information.
   * 
   * @param au
   *          ant-unification
   * @return the type information
   */
  public static RevisarTree<String> removeAll(final RevisarTree<String> au) {
    final List<String> list = new ArrayList<>();
    list.add("name_");
    return remove(au, list);
  }
  
  /**
   * Remove all.
   */
  public static String removeAll(final String template) {
    final RevisarTree<String> atree = RevisarTreeParser.parser(template);
    final RevisarTree<String> atreeplain = TemplateUtils.removeAll(atree);
    return EquationUtils.convertToEq(atreeplain);
  }
  
  /**
   * Contains any.
   * @param data data
   * @param toVerify list of data to verify
   * @return true if data contains any of the nodes to verify
   */
  public static boolean containsAny(final String data, final List<String> toVerify) {
    for (final String criterion : toVerify) {
      if (data.contains(criterion)) {
        return true;
      }
    }
    return false;
  }
}
