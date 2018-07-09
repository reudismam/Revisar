package br.ufcg.spg.cluster;

import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeParser;
import br.ufcg.spg.util.PrintUtils;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity(name = "Cluster")
@Table(name = "Cluster")
public class Cluster {
  @Id
  @GeneratedValue
  private Long id;
  /**
   * Unifier.
   */
  @Column(columnDefinition = "TEXT")
  private String au;

  /**
   * Cluster id.
   */
  private String label;

  /**
   * List of nodes in this cluster.
   */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Edit> nodes;
  
  /**
   * Destination cluster.
   */
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Cluster dst;
  
  public Cluster() {
  }

  /**
   * Constructor.
   */
  public Cluster(final String au, final String label) {
    this.au = au;
    this.label = label;
    this.nodes = new ArrayList<>();
  }

  /**
   * Gets anti-unification.
   * 
   * @return anti-unification
   */
  public String getAu() {
    return this.au;
  }

  /**
   * Sets anti-unification.
   * 
   * @param au anti-unification
   */
  public void setAu(final String au) {
    this.au = au;
  }

  /**
   * Gets the cluster id.
   * 
   * @return cluster id
   */
  public Long getId() {
    return id;
  }

  /**
   * Sets cluster id.
   * 
   * @param id - id
   */
  public void setId(final Long id) {
    this.id = id;
  }
  
  /**
   * Gets cluster label.
   * @return cluster label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets cluster label.
   * @param label label
   */
  public void setLabel(final String label) {
    this.label = label;
  }

  public List<Edit> getNodes() {
    return nodes;
  }

  public void setNodes(final List<Edit> nodes) {
    this.nodes = nodes;
  }

  public Cluster getDst() {
    return dst;
  }

  public void setDst(final Cluster dst) {
    this.dst = dst;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    final StringBuilder result = new StringBuilder(30);
    final RevisarTree<String> atree = RevisarTreeParser.parser(au);
    final String output =  PrintUtils.prettyPrint(atree);
    result.append(getLabel()).append('\n').append(output)
    .append("\nList of nodes ").append(nodes.size()).append(":\n\n");
    int count = 0;
    for (final Edit node : nodes) {
      result.append(node.getText()).append(", ")
      .append(node.getPath()).append(", ").append(node.getCommit()).append('\n');
      if (++count == 4) {
        result.append("...\n");
        break;
      }
    }
    return result.toString();
  }
}
