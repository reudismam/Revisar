package br.ufcg.spg.antiunification.algorithm;

import br.ufcg.spg.antiunification.AntiUnificationData;

public interface IAntiUnifyAlgoritm {

  /**
   * computes the anti-unification of two equations.
   */
  AntiUnificationData unify(String eq1, String eq2);

}