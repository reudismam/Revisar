package br.ufcg.spg.ml.editoperation;

import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.cluster.ClusterFormatter;
import de.jail.geometry.schemas.Point;

import java.util.ArrayList;
import java.util.List;

public class Script<T> extends Point {
  private List<EditNode<T>> list;
  
  private Cluster cluster;

  /**
   * Constructor.
   */
  public Script(List<EditNode<T>> edits, Cluster cluster) {
    super(new double[] {});
    this.list = edits;
    this.cluster = cluster;
  }

  public Script() {
    super(new double[] {});
    list = new ArrayList<>();
  }

  public List<EditNode<T>> getList() {
    return list;
  }

  public void setList(List<EditNode<T>> list) {
    this.list = list;
  }

  public Cluster getCluster() {
    return cluster;
  }

  public void setCluster(Cluster cluster) {
    this.cluster = cluster;
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
    @SuppressWarnings("unchecked")
    Script<T> other = (Script<T>) obj;
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
    return "Script [list=" + ClusterFormatter.formatList(list) + ", \ncluster="
      + cluster.getNodes().get(0) + "->" + cluster.getDst().getNodes().get(0) + "]\n\n";
  }
}
