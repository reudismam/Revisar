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
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((au == null) ? 0 : au.hashCode());
    result = prime * result + ((dst == null) ? 0 : dst.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((label == null) ? 0 : label.hashCode());
    result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
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
    Cluster other = (Cluster) obj;
    if (au == null) {
      if (other.au != null) {
        return false;
      }
    } else if (!au.equals(other.au)) {
      return false;
    }
    if (dst == null) {
      if (other.dst != null) {
        return false;
      }
    } else if (!dst.equals(other.dst)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (label == null) {
      if (other.label != null) {
        return false;
      }
    } else if (!label.equals(other.label)) {
      return false;
    }
    if (nodes == null) {
      if (other.nodes != null) {
        return false;
      }
    } else if (!nodes.equals(other.nodes)) {
      return false;
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    final StringBuilder result = new StringBuilder();
    final RevisarTree<String> atree = RevisarTreeParser.parser(au);
    final String output =  PrintUtils.prettyPrint(atree);
    result.append(getLabel()).append('\n').append(output)
    .append('\n').append(formatStringNodes());
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
  
  private String formatStringNodes() {
    StringBuilder result = new StringBuilder();
    result.append("List of nodes ").append(nodes.size()).append(":\n\n");
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
