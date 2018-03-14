package br.ufcg.spg.log;

/**
 * Time logger.
 */
public final class TimeLogger {
  
  /**
   * Singleton instance.
   */
  private static TimeLogger instance;
  
  /**
   * Time to extract pair.
   */
  private long timeExtract;
  
  /**
   * Time to extract dependence.
   */
  private long timeDependence;
  
  /**
   * Time to perform clustering.
   */
  private long timeCluster;
  
  /**
   * Time to transform.
   */
  private long timeTransform;
  
  private TimeLogger() {
  }
  
  /**
   * Gets singleton instance.
   * @return singleton instance
   */
  public static synchronized TimeLogger getInstance() {
    if (instance == null) {
      instance = new TimeLogger();
    }
    return instance;
  }

  public long getTimeExtract() {
    return timeExtract;
  }

  public void setTimeExtract(final long timeExtract) {
    this.timeExtract = timeExtract;
  }

  public long getTimeDependence() {
    return timeDependence;
  }

  public void setTimeDependence(final long timeDependence) {
    this.timeDependence = timeDependence;
  }

  public long getTimeCluster() {
    return timeCluster;
  }

  public void setTimeCluster(final long timeCluster) {
    this.timeCluster = timeCluster;
  }

  public long getTimeTransform() {
    return timeTransform;
  }

  public void setTimeTransform(final long timeTransform) {
    this.timeTransform = timeTransform;
  }
}
