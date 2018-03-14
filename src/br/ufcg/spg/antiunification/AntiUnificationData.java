package br.ufcg.spg.antiunification;

import at.jku.risc.stout.urauc.algo.AntiUnifyProblem.VariableWithHedges;

import java.util.List;

public class AntiUnificationData {
  /*
   * Unifier
   */
  private String unifier;

  /*
   * Variables
   */
  private List<VariableWithHedges> variables;

  public AntiUnificationData(String unifier, List<VariableWithHedges> variables) {
    this.unifier = unifier;
    this.variables = variables;
  }

  public String getUnifier() {
    return unifier;
  }

  public void setUnifier(String unifier) {
    this.unifier = unifier;
  }

  public List<VariableWithHedges> getVariables() {
    return variables;
  }

  public void setVariables(List<VariableWithHedges> variables) {
    this.variables = variables;
  }
  
  public int hashCode() {
    return toString().hashCode();
  }
  
  public String toString() {
    return unifier;
  }
}
