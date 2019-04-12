package br.ufcg.spg.match;

public class Match {
  
  /**
   * Source hash.
   */
  private String srcHash;
  
  /**
   * Destination hash.
   */
  private String dstHash;
  
  /**
   * Node value.
   */
  private String value;
  
  /**
   * Constructor.
   * @param srcHash source hash
   * @param dstHash destination hash
   * @param value value
   */
  public Match(final String srcHash, final String dstHash, final String value) {
    super();
    this.srcHash = srcHash;
    this.dstHash = dstHash;
    this.value = value;
  }

  public String getSrcHash() {
    return srcHash;
  }
  
  public void setSrcHash(final String srcHash) {
    this.srcHash = srcHash;
  }
  
  public String getDstHash() {
    return dstHash;
  }
  
  public void setDstHash(final String dstHash) {
    this.dstHash = dstHash;
  }
  
  public String getValue() {
    return value;
  }
  
  public void setValue(final String value) {
    this.value = value;
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj instanceof Match) {
      return false;
    }
    final Match other = (Match) obj;
    return this.srcHash.equals(other.getSrcHash()) && this.dstHash.equals(other.getDstHash());
  }
  
  @Override
  public int hashCode() {
    return toString().hashCode();
  }
  
  @Override
  public String toString() {
    return this.srcHash + " : " + this.dstHash;
  }
}
