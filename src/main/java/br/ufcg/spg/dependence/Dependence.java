package br.ufcg.spg.dependence;

import br.ufcg.spg.edit.Edit;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: Dependence.
 *
 */
@Entity(name = "Dependence")
@Table(name = "Dependence")
public class Dependence {
  @Id
  @GeneratedValue
  private Long id;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Edit edit;

  /**
   * List of nodes in this cluster.
   */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Edit> nodes;

  public Dependence() {
  }

  public Dependence(final Edit edit) {
    super();
    this.edit = edit;
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public Edit getEdit() {
    return edit;
  }

  public void setEdit(final Edit edit) {
    this.edit = edit;
  }

  public List<Edit> getNodes() {
    return nodes;
  }

  public void setNodes(final List<Edit> nodes) {
    this.nodes = nodes;
  }
  
  public void addNode(final Edit edit) {
    this.nodes.add(edit);
  }
}
