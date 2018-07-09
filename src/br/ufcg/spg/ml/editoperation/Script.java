package br.ufcg.spg.ml.editoperation;

import br.ufcg.spg.cluster.Cluster;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Clusterable;

public class Script implements Clusterable {
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((cluster == null) ? 0 : cluster.hashCode());
    result = prime * result + ((list == null) ? 0 : list.hashCode());
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
    Script other = (Script) obj;
    if (cluster == null) {
      if (other.cluster != null) {
        return false;
      }
    } else if (!cluster.equals(other.cluster)) {
      return false;
    }
    if (list == null) {
      if (other.list != null) {
        return false;
      }
    } else if (!list.equals(other.list)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Script [list=" + list + ", \ncluster="
      + cluster.getNodes().get(0) + "->" + cluster.getDst().getNodes().get(0) + "]\n\n";
  }
}
