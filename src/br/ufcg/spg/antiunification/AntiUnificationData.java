package br.ufcg.spg.antiunification;

import br.ufcg.spg.antiunification.substitution.HoleWithSubstutings;

import java.util.List;

public class AntiUnificationData {
  /*
   * Unifier
   */
  private String unifier;

  /*
   * Variables
   */
  private List<HoleWithSubstutings> variables;

  public AntiUnificationData(String unifier, List<HoleWithSubstutings> variables) {
    this.unifier = unifier;
    this.variables = variables;
  }

  public String getUnifier() {
    return unifier;
  }

  public void setUnifier(String unifier) {
    this.unifier = unifier;
  }

  public List<HoleWithSubstutings> getVariables() {
    return variables;
  }

  public void setVariables(List<HoleWithSubstutings> variables) {
    this.variables = variables;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((unifier == null) ? 0 : unifier.hashCode());
    result = prime * result + ((variables == null) ? 0 : variables.hashCode());
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AntiUnificationData other = (AntiUnificationData) obj;
    if (unifier == null) {
      if (other.unifier != null) {
        return false;
      }
    } else if (!unifier.equals(other.unifier)) {
      return false;
    }
    if (variables == null) {
      if (other.variables != null) {
        return false;
      }
    } else if (!variables.equals(other.variables)) {
      return false;
    }
    return true;
  }

  public String toString() {
    return unifier;
  }
}
