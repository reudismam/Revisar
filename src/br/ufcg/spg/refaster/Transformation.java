package br.ufcg.spg.refaster;

import br.ufcg.spg.cluster.Cluster;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity(name = "Transformation")
@Table(name = "Transformation")
public class Transformation {
  @Id
  @GeneratedValue
  private Long id;
  
  @Column(columnDefinition = "TEXT")
  private String transformation;
  
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Cluster cluster;
  
  private boolean valid;

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getTransformation() {
    return transformation;
  }

  public void setTransformation(final String transformation) {
    this.transformation = transformation;
  }

  public Cluster getCluster() {
    return cluster;
  }

  public void setCluster(final Cluster cluster) {
    this.cluster = cluster;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(final boolean valid) {
    this.valid = valid;
  }
}
