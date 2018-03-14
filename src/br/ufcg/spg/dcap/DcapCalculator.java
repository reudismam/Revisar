package br.ufcg.spg.dcap;

import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.tree.RevisarTreeParser;
import br.ufcg.spg.tree.RevisarTree;

import org.eclipse.jdt.core.dom.ASTNode;

public class DcapCalculator {
  /**
   * Gets d-cap for anti-unification.
   * @param au anti-unification
   * @param d dimension
   * @param index index of the place-hold
   * @return d-cap for anti-unification
   */
  private static RevisarTree<String> dcap(final RevisarTree<String> au, int d, int index) {
    if (d == 0 || au.getChildren().isEmpty()) {
      return new RevisarTree<>("#" + index);
    }
    final RevisarTree<String> atree = new RevisarTree<>(au.getValue());
    d--;
    for (final RevisarTree<String> child : au.getChildren()) {
      final RevisarTree<String> achild = dcap(child, d, index++);
      atree.addChild(achild);
    }
    return atree;
  }
  
  /**
   * Gets d-cap for anti-unification.
   * @param au anti-unification
   * @return d-cap for anti-unification
   */
  public static RevisarTree<String> dcap(final AntiUnifier au, final int d) {
    final String auEquation = EquationUtils.convertToEquation(au);
    final RevisarTree<String> atree = RevisarTreeParser.parser(auEquation);
    return dcap(atree, d, 1);
  }
  
  /**
   * Gets d-cap for anti-unification.
   * @param au anti-unification
   * @return d-cap for anti-unification
   */
  public static RevisarTree<String> dcap(final ASTNode au) {
    final int d = 3;
    final String auEquation = EquationUtils.convertToAuEq(au);
    final RevisarTree<String> atree = RevisarTreeParser.parser(auEquation);
    return dcap(atree, d, 1);
  }

}
