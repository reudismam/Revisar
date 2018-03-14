package br.ufcg.spg.template;

import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.tree.AParser;
import br.ufcg.spg.tree.ATree;

import java.util.ArrayList;
import java.util.List;

public class TemplateUtils {

  /**
   * Removes information.
   * 
   * @param au
   *          ant-unification
   * @param toRemove nodes to be removed.
   * @return the type information
   */
  private static ATree<String> remove(final ATree<String> au, final List<String> toRemove) {
    if (au.getChildren().isEmpty()) {
      if (!containsAny(au.getValue(), toRemove)) {
        return au;
      }
      return null;
    }
    final ATree<String> newAu = new ATree<String>(au.getValue());
    for (final ATree<String> aui : au.getChildren()) {
      final ATree<String> rau = remove(aui, toRemove);
      if (rau != null) {
        newAu.getChildren().add(rau);
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
  public static ATree<String> removeType(final ATree<String> au) {
    final List<String> list = new ArrayList<>();
    list.add("type_");
    return remove(au, list);
  }
  
  public static String removeType(final String template) {
    final ATree<String> atree = AParser.parser(template);
    final ATree<String> atreeplain = TemplateUtils.removeType(atree);
    final String srcEq = EquationUtils.convertToEq(atreeplain);
    return srcEq;
  }
  
  /**
   * Removes the type information.
   * 
   * @param au
   *          ant-unification
   * @return the type information
   */
  public static ATree<String> removeName(final ATree<String> au) {
    final List<String> list = new ArrayList<>();
    list.add("name_");
    return remove(au, list);
  }
  
  
  public static String removeName(final String template) {
    final ATree<String> atree = AParser.parser(template);
    final ATree<String> atreeplain = TemplateUtils.removeName(atree);
    final String srcEq = EquationUtils.convertToEq(atreeplain);
    return srcEq;
  }
  
  /**
   * Removes the type information.
   * 
   * @param au
   *          ant-unification
   * @return the type information
   */
  public static ATree<String> removeAll(final ATree<String> au) {
    final List<String> list = new ArrayList<>();
    list.add("name_");
    return remove(au, list);
  }
  
  public static String removeAll(final String template) {
    final ATree<String> atree = AParser.parser(template);
    final ATree<String> atreeplain = TemplateUtils.removeAll(atree);
    final String srcEq = EquationUtils.convertToEq(atreeplain);
    return srcEq;
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
