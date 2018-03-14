package br.ufcg.spg.dcap;

import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.tree.AParser;
import br.ufcg.spg.tree.ATree;

import org.eclipse.jdt.core.dom.ASTNode;

public class DcapCalculator {
  /**
   * Gets d-cap for anti-unification.
   * @param au anti-unification
   * @param d dimension
   * @param index index of the place-hold
   * @return d-cap for anti-unification
   */
  private static ATree<String> dcap(final ATree<String> au, int d, int index) {
    if (d == 0 || au.getChildren().isEmpty()) {
      return new ATree<>("#" + index);
    }
    final ATree<String> atree = new ATree<>(au.getValue());
    d--;
    for (final ATree<String> child : au.getChildren()) {
      final ATree<String> achild = dcap(child, d, index++);
      atree.addChild(achild);
    }
    return atree;
  }
  
  /**
   * Gets d-cap for anti-unification.
   * @param au anti-unification
   * @return d-cap for anti-unification
   */
  public static ATree<String> dcap(final AntiUnifier au, final int d) {
    final String auEquation = EquationUtils.convertToEquation(au);
    final ATree<String> atree = AParser.parser(auEquation);
    return dcap(atree, d, 1);
  }
  
  /**
   * Gets d-cap for anti-unification.
   * @param au anti-unification
   * @return d-cap for anti-unification
   */
  public static ATree<String> dcap(final ASTNode au) {
    final int d = 3;
    final String auEquation = EquationUtils.convertToAuEq(au);
    final ATree<String> atree = AParser.parser(auEquation);
    return dcap(atree, d, 1);
  }

}
