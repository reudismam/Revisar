package br.ufcg.spg.database;

import br.ufcg.spg.dependence.Dependence;
import br.ufcg.spg.edit.Edit;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public final class DependenceDao extends GenericDao<Dependence, Long> {
  
  private static DependenceDao instance;

  private DependenceDao() {
    super(Dependence.class);
  }

  /**
   * Gets singleton instance.
   * @return singleton instance
   */
  public static synchronized DependenceDao getInstance() {
    if (instance == null) {
      instance = new DependenceDao();
    }
    return instance;
  }

  /**
   * Gets last edit.
   * 
   * @return source code edits.
   */
  public Edit lastDependence() {
    final CriteriaBuilder builder = em.getCriteriaBuilder();
    final CriteriaQuery<Dependence> query = builder.createQuery(Dependence.class);
    final Root<Dependence> root = query.from(Dependence.class);
    query.orderBy(builder.desc(root.get("id")));
    final List<Dependence> list = em.createQuery(query).setMaxResults(1).getResultList();
    if (list.isEmpty()) {
      return null;
    } else {
      return list.get(0).getEdit();
    }
  }
}
