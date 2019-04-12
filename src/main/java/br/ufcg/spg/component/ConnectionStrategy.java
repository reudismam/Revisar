package br.ufcg.spg.component;

public interface ConnectionStrategy {
  /**
   * Verifies whether two nodes are connected.
   */
  public boolean isConnected(final int indexI, final int indexJ);
}
