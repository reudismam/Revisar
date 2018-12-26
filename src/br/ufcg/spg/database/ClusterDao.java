package br.ufcg.spg.database;

import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.filter.FilterManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class ClusterDao extends GenericDao<Cluster, String> {
  private static ClusterDao instance;
  
  private ClusterDao() {
    super(Cluster.class);
  }  
  
  /**
   * Gets singleton instance.
   * @return singleton instance
   */
  public static synchronized ClusterDao getInstance() {
    if (instance == null) {
      instance = new ClusterDao();
    }
    return instance;
  }
  
  /**
   * Gets clusters.
   * @return clusters
   */
  public List<Cluster> getSrcClusters() {
    final CriteriaBuilder builder = em.getCriteriaBuilder();
    final CriteriaQuery<Cluster> query = builder.createQuery(Cluster.class);
    final Root<Cluster> root = query.from(Cluster.class);
    final Expression<Cluster> dst = root.get("dst");
    final Predicate n1 = builder.isNotNull(dst);
    query.where(n1);
    query.orderBy(builder.asc(root.get("id")));
    return getAll(query);
  }
  
  /**
   * Gets cluster by label.
   * @return cluster for the label
   */
  public List<Cluster> getClusters(final String srcClusterId) {
    final CriteriaBuilder builder = em.getCriteriaBuilder();
    final CriteriaQuery<Cluster> query = builder.createQuery(Cluster.class);
    final Root<Cluster> root = query.from(Cluster.class);
    final Expression<Cluster> dst = root.get("dst");
    final Expression<String> label = root.get("id");
    final Predicate n1 = builder.isNotNull(dst);
    final Predicate n2 = builder.equal(label, srcClusterId);
    query.where(builder.and(n1, n2));
    return getAll(query);
  }
  
  /**
   * Gets source code edits.
   * @return source code edits.
   */
  public int getMaxLabel() {
    final String query = "SELECT DISTINCT(e.label) FROM Cluster e WHERE e.dst IS NOT NULL "
        + "ORDER BY cast(e.label as integer) desc";
    final List<String> list = em.createQuery(query, String.class).getResultList();
    if (list.isEmpty()) {
      return -1;
    } else {
      return Integer.parseInt(list.get(0));
    }
  }
  
  /**
   * Gets source code edits.
   * @return source code edits.
   */
  public String getLastDcap(final int d) {
    final String query = "SELECT e.dcap" + d + " FROM Cluster c JOIN fetch c.nodes e"
                      + " WHERE c.dst IS NOT NULL"
                      + " GROUP BY c.id, c.label, e.dcap" + d
                      + " ORDER BY CAST(c.label as integer) desc";
    final List<String> list = em.createQuery(query, String.class).getResultList();
    if (list.isEmpty()) {
      return null;
    } else {
      return list.get(0);
    }
  }
  
  /**
   * Gets source code edits.
   * @return source code edits.
   */
  public List<Cluster> getLargestClusters() {
    final String query = "select t.label from "
                          + "(select c.label, count(*) as s from cluster c, cluster_edit ce "
                          + "where c.id = ce.cluster_id and "
                          + "c.dst_id is not null "
                          + "group by c.label) as t "
                          + "where s >= 15 "
                          + "order by s desc";
    final Query q = em.createNativeQuery(query);
    @SuppressWarnings("unchecked")
    final List<String> list = q.getResultList();
    final List<Cluster> clist = new ArrayList<Cluster>();
    for (int i = 0; i < list.size(); i++) {
      final Cluster c = this.getClusters(list.get(i)).get(0);
      clist.add(c);
    }
    return clist;
  }
  
  /**
   * Gets source code edits.
   * @return source code edits.
   */
  public List<Cluster> getClusterMoreProjects(int numberProjects) {
    final String query = "select t.id from "
                          + "(select c.id, count(distinct(e.project)) as s "
                          + "from cluster c, cluster_edit ce, edit e "
                          + "where c.id = ce.cluster_id and "
                          + "ce.nodes_id = e.id and "
                          + "c.dst_id is not null and "
                          + "e.dst_id is not null and "
                          + "e.context is not null "
                          + "group by c.id) as t "
                          + "where s >= " + numberProjects + " "
                          + "order by s desc, t.id asc";
    final Query q = em.createNativeQuery(query);
    @SuppressWarnings("unchecked")
    final List<Long> list = q.getResultList();
    final List<Cluster> clist = new ArrayList<Cluster>();
    for (int i = 0; i < list.size(); i++) {
      final Cluster c = this.getClusters(list.get(i) + "").get(0);
      clist.add(c);
    }
    return clist;
  }
  
  /**
   * Gets source code edits.
   * @return source code edits.
   */
  public List<String> getAllCommitsClusters() {
    final String query = "select s from "
                          + "(select c.label, e.commit as s from cluster c, "
                          + "cluster_edit ce, edit e "
                          + "where c.id = ce.cluster_id and "
                          + "ce.nodes_id = e.id and "
                          + "c.dst_id is not null and "
                          + "e.dst_id is not null and "
                          + "e.context is not null "
                          + "group by c.label, e.commit, e.id "
                          + "ORDER BY e.id) as t ";
    final Query q = em.createNativeQuery(query);
    @SuppressWarnings("unchecked")
    final List<String> list = q.getResultList();
    final List<String> newList = new ArrayList<>();
    final HashSet<String> set = new HashSet<>();
    for (final String s : list) {
      if (!set.contains(s)) {
        set.add(s);
        newList.add(s);
      }
    }
    return newList;
  }

  /**
   * Get clusters with the largest number of examples.
   */
  public static List<Cluster> getClusterMoreProjects() {
    final ClusterDao dao = getInstance();
    List<Cluster> clusters = new ArrayList<>(dao.getClusterMoreProjects(3));
    List<Cluster> newList = new ArrayList<>();
    for (Cluster cluster : clusters) {
      if (!FilterManager.isSameBeforeAfter(cluster)) {
        newList.add(cluster); 
      }
    }
    return newList;
  }
}
