package br.ufcg.spg.antiunification.algorithm;

import at.jku.risc.stout.urauc.algo.AlignFnc;
import at.jku.risc.stout.urauc.algo.AntiUnify;
import at.jku.risc.stout.urauc.algo.AntiUnifyProblem;
import at.jku.risc.stout.urauc.algo.AntiUnifyProblem.VariableWithHedges;
import at.jku.risc.stout.urauc.algo.AntiUnifySystem;
import at.jku.risc.stout.urauc.algo.DebugLevel;
import at.jku.risc.stout.urauc.data.EquationSystem;
import at.jku.risc.stout.urauc.data.Hedge;
import at.jku.risc.stout.urauc.data.atom.Variable;
import br.ufcg.spg.antiunification.AntiUnificationData;
import br.ufcg.spg.antiunification.substitution.HoleWithSubstutings;

import java.util.ArrayList;
import java.util.List;

public class AntiUnifierHolesArauc extends AntiUnify {

  private final List<AntiUnificationData> unifications;

  public AntiUnifierHolesArauc(final AlignFnc func, final EquationSystem<AntiUnifyProblem> sys, 
      final DebugLevel debugLevel) {
    super(func, sys, debugLevel);
    unifications = new ArrayList<>();
  }

  @Override
  public void callback(final AntiUnifySystem res, final Variable var) {
    final Hedge hedge = res.getSigma().get(var);
    final List<VariableWithHedges> S = res.getStoreHorizontalS();
    final List<VariableWithHedges> Q = res.getStoreVerticalQ();
    final List<HoleWithSubstutings> holes = convertToHoleWithSubstutings(S);
    final List<HoleWithSubstutings> qholes = convertToHoleWithSubstutings(Q);
    holes.addAll(qholes);
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
  
  private List<HoleWithSubstutings> convertToHoleWithSubstutings(
      final List<VariableWithHedges> variables) {
    final List<HoleWithSubstutings> list = new ArrayList<>();
    for (final VariableWithHedges var : variables) {
      final HoleWithSubstutings hole = new HoleWithSubstutings();
      hole.setHole(var.getVar().toString());
      hole.setLeftSubstuting(var.getLeft().toString());
      hole.setRightSubstuting(var.getRight().toString());
      list.add(hole);
    }
    return list;
  }
}