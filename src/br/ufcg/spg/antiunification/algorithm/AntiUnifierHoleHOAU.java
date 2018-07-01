package br.ufcg.spg.antiunification.algorithm;

import at.jku.risc.stout.hoau.algo.AntiUnify;
import at.jku.risc.stout.hoau.algo.AntiUnifyProblem;
import at.jku.risc.stout.hoau.algo.AntiUnifySystem;
import at.jku.risc.stout.hoau.algo.DebugLevel;
import at.jku.risc.stout.hoau.data.EquationSystem;
import at.jku.risc.stout.hoau.data.TermNode;
import at.jku.risc.stout.hoau.data.atom.Variable;
import at.jku.risc.stout.hoau.util.ControlledException;
import br.ufcg.spg.antiunification.AntiUnificationData;
import br.ufcg.spg.antiunification.substitution.HoleWithSubstutings;

import java.util.ArrayList;
import java.util.List;

public class AntiUnifierHoleHOAU extends AntiUnify {

  private final List<AntiUnificationData> unifications;

  public AntiUnifierHoleHOAU(final EquationSystem<AntiUnifyProblem> sys, int maxReduce,
      final DebugLevel debugLevel) throws ControlledException {
    super(sys, maxReduce, debugLevel);
    unifications = new ArrayList<>();
  }

  @Override
  public void callback(final AntiUnifySystem res, final Variable var) {
    final TermNode hedge = res.getSigma().get(var);
    List<AntiUnifyProblem> store = res.getStore();
    List<HoleWithSubstutings> holes = new ArrayList<>();
    for (AntiUnifyProblem p : store) {
      HoleWithSubstutings hole = new HoleWithSubstutings();
      hole.setHole(p.getGeneralizationVar().toString());
      hole.setLeftSubstuting(p.getLeft().toString());
      hole.setRightSubstuting(p.getRight().toString());
      holes.add(hole);
    }
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