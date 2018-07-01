package br.ufcg.spg.antiunification.dist;

import br.ufcg.spg.antiunification.substitution.HoleWithSubstutings;

public class RightDistanceCalculator extends DistanceCalculator {

  @Override
  public String getUnifier(final HoleWithSubstutings root) {
    return root.getRightSubstuting();
  }
}
