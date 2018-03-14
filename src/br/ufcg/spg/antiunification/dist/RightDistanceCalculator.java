package br.ufcg.spg.antiunification.dist;

import at.jku.risc.stout.urauc.algo.AntiUnifyProblem.VariableWithHedges;
import at.jku.risc.stout.urauc.data.Hedge;

public class RightDistanceCalculator extends DistanceCalculator {

  @Override
  public Hedge getUnifier(final VariableWithHedges root) {
    return root.getRight();
  }
}
