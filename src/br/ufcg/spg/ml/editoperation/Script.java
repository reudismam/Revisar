package br.ufcg.spg.ml.editoperation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Clusterable;

import br.ufcg.spg.cluster.Cluster;

public class Script implements Clusterable{
  private List<IEditNode> list;
  
  private Cluster cluster;

  public Script(List<IEditNode> edits, Cluster cluster) {
    this.list = edits;
    this.cluster = cluster;
  }

  public Script() {
    list = new ArrayList<>();
  }

  public List<IEditNode> getList() {
    return list;
  }

  public void setList(List<IEditNode> list) {
    this.list = list;
  }

  public Cluster getCluster() {
    return cluster;
  }

  public void setCluster(Cluster cluster) {
    this.cluster = cluster;
  }

  @Override
  public double[] getPoint() {
    return null;
  }

  @Override
  public String toString() {
    return "Script [list=" + list + ", \ncluster=" + cluster.getNodes().get(0) + "->" + cluster.getDst().getNodes().get(0) + "]\n\n";
  }
}
