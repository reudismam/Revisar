package br.ufcg.spg.bean;

/**
 * Represents a tuple.
 * @param <X> type of the first item
 * @param <Y> type of the second item
 */
public class Tuple<X, Y> {
  /**
   * First item.
   */
  private X item1;
  
  /**
   * Second item.
   */
  private Y item2;

  /**
   * Constructs a new tuple.
   * 
   * @param item1
   *          first item.
   * @param item2
   *          second item.
   */
  public Tuple(final X item1, final Y item2) {
    this.item1 = item1;
    this.item2 = item2;
  }
  
  public X getItem1() {
    return item1;
  }

  public void setItem1(final X item1) {
    this.item1 = item1;
  }

  public Y getItem2() {
    return item2;
  }
  
  public void setItem2(final Y item2) {
    this.item2 = item2;
  }

  @Override
  public String toString() {
    return item1.toString() + ", " + item2.toString();
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == null || !(obj instanceof Tuple)) {
      return false;
    }
    @SuppressWarnings("unchecked")
    final Tuple<X, Y> other = (Tuple<X, Y>) obj;
    if (!this.getItem1().equals(other.getItem1())) {
      return false;
    }
    if (!this.getItem2().equals(other.getItem2())) {
      return false;
    }
    return true;
  }
}