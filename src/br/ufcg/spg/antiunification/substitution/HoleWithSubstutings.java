package br.ufcg.spg.antiunification.substitution;

public class HoleWithSubstutings {
  /**
   * Hole.
   */
  private String hole;

  /**
   * Left substuting.
   */
  private String leftSubstuting;

  /**
   * Right substuting.
   */
  private String rightSubstuting;

  /**
   * Constructor.
   */
  public HoleWithSubstutings(String hole, String leftSubstuting, String rightSubstuting) {
    this.hole = hole;
    this.leftSubstuting = leftSubstuting;
    this.rightSubstuting = rightSubstuting;
  }

  public HoleWithSubstutings() {
  }

  public String getHole() {
    return hole;
  }

  public void setHole(String hole) {
    this.hole = hole;
  }

  public String getLeftSubstuting() {
    return leftSubstuting;
  }

  public void setLeftSubstuting(String leftSubstuting) {
    this.leftSubstuting = leftSubstuting;
  }

  public String getRightSubstuting() {
    return rightSubstuting;
  }

  public void setRightSubstuting(String rightSubstuting) {
    this.rightSubstuting = rightSubstuting;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((hole == null) ? 0 : hole.hashCode());
    result = prime * result + ((leftSubstuting == null) ? 0 : leftSubstuting.hashCode());
    result = prime * result + ((rightSubstuting == null) ? 0 : rightSubstuting.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    HoleWithSubstutings other = (HoleWithSubstutings) obj;
    if (hole == null) {
      if (other.hole != null)
        return false;
    } else if (!hole.equals(other.hole))
      return false;
    if (leftSubstuting == null) {
      if (other.leftSubstuting != null)
        return false;
    } else if (!leftSubstuting.equals(other.leftSubstuting))
      return false;
    if (rightSubstuting == null) {
      if (other.rightSubstuting != null)
        return false;
    } else if (!rightSubstuting.equals(other.rightSubstuting))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return hole + " : " + leftSubstuting + " =^= "+ rightSubstuting;
  }
}
