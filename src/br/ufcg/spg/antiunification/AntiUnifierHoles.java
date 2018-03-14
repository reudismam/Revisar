package br.ufcg.spg.antiunification;

import at.jku.risc.stout.urauc.algo.AlignFnc;
import at.jku.risc.stout.urauc.algo.AntiUnify;
import at.jku.risc.stout.urauc.algo.AntiUnifyProblem;
import at.jku.risc.stout.urauc.algo.AntiUnifyProblem.VariableWithHedges;
import at.jku.risc.stout.urauc.algo.AntiUnifySystem;
import at.jku.risc.stout.urauc.algo.DebugLevel;
import at.jku.risc.stout.urauc.data.EquationSystem;
import at.jku.risc.stout.urauc.data.Hedge;
import at.jku.risc.stout.urauc.data.atom.Variable;

import java.util.ArrayList;
import java.util.List;

public class AntiUnifierHoles extends AntiUnify {

  private final List<AntiUnificationData> unifications;

  public AntiUnifierHoles(final AlignFnc aFnc, final EquationSystem<AntiUnifyProblem> sys, 
      final DebugLevel debugLevel) {
    super(aFnc, sys, debugLevel);
    unifications = new ArrayList<AntiUnificationData>();
  }

  @Override
  public void callback(final AntiUnifySystem res, final Variable var) {
    final Hedge hedge = res.getSigma().get(var);
    final List<VariableWithHedges> S = res.getStoreHorizontalS();
    final List<VariableWithHedges> Q = res.getStoreVerticalQ();
    final List<VariableWithHedges> variables = new ArrayList<VariableWithHedges>(S);
    variables.addAll(Q);
    final AntiUnificationData data = new AntiUnificationData(hedge.toString(), variables);
    unifications.add(data);
  }

  /**
   * Gets the ant-unification
   * @return the unification.
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