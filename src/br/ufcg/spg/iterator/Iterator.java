package br.ufcg.spg.iterator;

public interface Iterator<T> {
  /**
   * Determines if there are nodes to analyze
   * 
   * @return
   */
  public boolean hasNext();

  /**
   * Gets next node to analyze
   * 
   * @return
   */
  public T next();
}
