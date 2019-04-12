package br.ufcg.spg.antiunification.algorithm;

import at.jku.risc.stout.urau.algo.AntiUnifyProblem;
import at.jku.risc.stout.urau.algo.DebugLevel;
import at.jku.risc.stout.urau.algo.RigidityFnc;
import at.jku.risc.stout.urau.algo.RigidityFncSubsequence;
import at.jku.risc.stout.urau.data.EquationSystem;
import at.jku.risc.stout.urau.data.InputParser;

import br.ufcg.spg.antiunification.AntiUnificationData;

import java.io.Reader;
import java.io.StringReader;

public class Urau implements IAntiUnifyAlgoritm {

  /**
   * {@inheritDoc}
   **/
  @Override
  public AntiUnificationData unify(final String eq1, final String eq2) {
    final Reader in1 = new StringReader(eq1);
    final Reader in2 = new StringReader(eq2);
    boolean iterateAll = true;

    RigidityFnc func = new RigidityFncSubsequence().setMinLen(3);
    EquationSystem<AntiUnifyProblem> eqSys = new EquationSystem<AntiUnifyProblem>() {
        public AntiUnifyProblem newEquation() {
            return new AntiUnifyProblem();
        }
    };
    try {
      new InputParser<AntiUnifyProblem>(eqSys).parseHedgeEquation(in1, in2);
      final AntiUnifierHolesArau antUnifier = new AntiUnifierHolesArau(
          func, eqSys, DebugLevel.SILENT);
      antUnifier.antiUnify(iterateAll, System.out);
      return antUnifier.getUnification();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
