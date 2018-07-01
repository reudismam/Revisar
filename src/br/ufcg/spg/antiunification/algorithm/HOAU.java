package br.ufcg.spg.antiunification.algorithm;

import at.jku.risc.stout.hoau.algo.AntiUnifyProblem;
import at.jku.risc.stout.hoau.algo.DebugLevel;
import at.jku.risc.stout.hoau.data.EquationSystem;
import at.jku.risc.stout.hoau.data.InputParser;

import br.ufcg.spg.antiunification.AntiUnificationData;

import java.io.Reader;
import java.io.StringReader;

public class HOAU implements IAntiUnifyAlgoritm {

  /**
   * {@inheritDoc}
   **/
  @Override
  public AntiUnificationData unify(final String eq1, final String eq2) {
    final Reader in1 = new StringReader(eq1);
    final Reader in2 = new StringReader(eq2);
    int maxReduce = 100;

    EquationSystem<AntiUnifyProblem> eqSys = new EquationSystem<AntiUnifyProblem>() {
        public AntiUnifyProblem newEquation() {
            return new AntiUnifyProblem();
        }
    };
    try {
      new InputParser().parseEquation(in1, in2, eqSys, maxReduce);
      final AntiUnifierHoleHOAU antUnifier = new AntiUnifierHoleHOAU(
           eqSys, maxReduce, DebugLevel.SILENT);
      antUnifier.antiUnify(false, System.out);
      return antUnifier.getUnification();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}