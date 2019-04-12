package br.ufcg.spg.antiunification.algorithm;

import at.jku.risc.stout.urauc.algo.AlignFnc;
import at.jku.risc.stout.urauc.algo.AlignFncLAA;
import at.jku.risc.stout.urauc.algo.AntiUnifyProblem;
import at.jku.risc.stout.urauc.algo.DebugLevel;
import at.jku.risc.stout.urauc.data.EquationSystem;
import at.jku.risc.stout.urauc.data.InputParser;
import at.jku.risc.stout.urauc.util.ControlledException;
import br.ufcg.spg.antiunification.AntiUnificationData;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class Urauc implements IAntiUnifyAlgoritm {

  /**
   * {@inheritDoc}
   **/
  @Override
  public AntiUnificationData unify(final String eq1, final String eq2) {
    final Reader in1 = new StringReader(eq1);
    final Reader in2 = new StringReader(eq2);
    final boolean iterateAll = true;
    final AlignFnc alFnc = new AlignFncLAA();
    final EquationSystem<AntiUnifyProblem> eqSys = new EquationSystem<AntiUnifyProblem>() {
      @Override
      public AntiUnifyProblem newEquation() {
        return new AntiUnifyProblem();
      }
    };
    try {
      new InputParser<AntiUnifyProblem>(eqSys).parseHedgeEquation(in1, in2);
      final AntiUnifierHolesArauc antUnifier = new AntiUnifierHolesArauc(
          alFnc, eqSys, DebugLevel.SILENT);
      antUnifier.antiUnify(iterateAll, false, System.out);
      return antUnifier.getUnification();
    } catch (IOException | ControlledException e) {
      throw new RuntimeException(e);
    }
  }
}