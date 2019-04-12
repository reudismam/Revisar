package br.ufcg.spg.excel;

import br.ufcg.spg.cli.PatternStatus;
import br.ufcg.spg.cluster.Cluster;

public class QuickFix {
  private int id;

  private Cluster cluster;

  private PatternStatus status;

  public QuickFix() {
    this.status = PatternStatus.UNDEFINED;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Cluster getCluster() {
    return cluster;
  }

  public void setCluster(Cluster cluster) {
    this.cluster = cluster;
  }

  public PatternStatus getStatus() {
    return status;
  }

  public void setStatus(PatternStatus status) {
    this.status = status;
  }
}
