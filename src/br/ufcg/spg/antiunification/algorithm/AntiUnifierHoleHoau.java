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
import java.util.regex.Pattern;

public class AntiUnifierHoleHoau extends AntiUnify {

  private final List<AntiUnificationData> unifications;

  public AntiUnifierHoleHoau(final EquationSystem<AntiUnifyProblem> sys, int maxReduce,
      final DebugLevel debugLevel) throws ControlledException {
    super(sys, maxReduce, debugLevel);
    unifications = new ArrayList<>();
  }

  @Override
  public void callback(final AntiUnifySystem res, final Variable var) {
    final TermNode hedge = res.getSigma().get(var);
    List<AntiUnifyProblem> store = res.getStore();
    List<HoleWithSubstutings> holes = new ArrayList<>();
    String regex = Pattern.quote("$");
    for (AntiUnifyProblem p : store) {
      HoleWithSubstutings hole = new HoleWithSubstutings();
      String generalizedVariable = p.getGeneralizationVar().toString().replaceAll(regex, "#");
      hole.setHole(generalizedVariable);
      String leftSub = p.getLeft().toString().replaceAll(regex, "#");
      hole.setLeftSubstuting(leftSub);
      String rightSub = p.getRight().toString().replaceAll(regex, "#");
      hole.setRightSubstuting(rightSub);
      holes.add(hole);
    }
    String hedgeStr = hedge.toString().replaceAll(regex, "#");
    final AntiUnificationData data = new AntiUnificationData(hedgeStr, holes);
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