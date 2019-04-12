package br.ufcg.spg.antiunification.algorithm;

import at.jku.risc.stout.urau.algo.AntiUnify;
import at.jku.risc.stout.urau.algo.AntiUnifyProblem;
import at.jku.risc.stout.urau.algo.AntiUnifySystem;
import at.jku.risc.stout.urau.algo.DebugLevel;
import at.jku.risc.stout.urau.algo.RigidityFnc;
import at.jku.risc.stout.urau.data.EquationSystem;
import at.jku.risc.stout.urau.data.TermNode;
import at.jku.risc.stout.urau.data.atom.Variable;

import br.ufcg.spg.antiunification.AntiUnificationData;
import br.ufcg.spg.antiunification.substitution.HoleWithSubstutings;

import java.util.ArrayList;
import java.util.List;

public class AntiUnifierHolesArau extends AntiUnify {

  private final List<AntiUnificationData> unifications;

  public AntiUnifierHolesArau(final RigidityFnc func, final EquationSystem<AntiUnifyProblem> sys, 
      final DebugLevel debugLevel) {
    super(func, sys, debugLevel);
    unifications = new ArrayList<>();
  }

  @Override
  public void callback(final AntiUnifySystem res, final Variable var) {
    final TermNode hedge = res.getSigma().get(var);
    final List<HoleWithSubstutings> holes = new ArrayList<>();
    final AntiUnificationData data = new AntiUnificationData(hedge.toString(), holes);
    unifications.add(data);
  }

  /**
   * Gets the ant-unification.
   * @return the unification
   */
  public AntiUnificationData getUnification() {
    return unifications.get(0);
  }

  /**
   * Gets all ant-unifications.
   * @return the unification.
   */
  public List<AntiUnificationData> getUnifications() {
    return unifications;
  }
}