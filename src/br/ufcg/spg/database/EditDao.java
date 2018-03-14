package br.ufcg.spg.database;

import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.imports.Import;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class EditDao extends GenericDao<Edit, Long> {
  private static EditDao instance;
  
  private EditDao() {
    super(Edit.class);
  }  
  
  /**
   * Gets singleton instance.
   * @return singleton instance.
   */
  public static synchronized EditDao getInstance() {
    if (instance == null) {
      instance = new EditDao();
    }
    return instance;
  }
  
  /**
   * Gets source code edits.
   * @return source code edits.
   */
  public List<Edit> getSrcEdits() {
    final CriteriaBuilder builder = em.getCriteriaBuilder();
    final CriteriaQuery<Edit> query = builder.createQuery(Edit.class);
    final Root<Edit> root = query.from(Edit.class);
    final Expression<Edit> context = root.get("context");
    final Expression<Edit> dst = root.get("dst");
    final Predicate n1 = builder.isNotNull(context);
    final Predicate n2 = builder.isNotNull(dst);
    query.where(builder.and(n1, n2));
    query.orderBy(builder.asc(root.get("id")));
    return getAll(query);
  }
  
  /**
   * Gets source code edits.
   * @return source code edits.
   */
  public List<Edit> getSrcEdits(final String commitStr) {
    final CriteriaBuilder builder = em.getCriteriaBuilder();
    final CriteriaQuery<Edit> query = builder.createQuery(Edit.class);
    final Root<Edit> root = query.from(Edit.class);
    final Expression<Edit> context = root.get("context");
    final Expression<Edit> dst = root.get("dst");
    final Expression<String> commit = root.get("commit");
    final Predicate n1 = builder.isNotNull(context);
    final Predicate n2 = builder.isNotNull(dst);
    final Predicate n3 = builder.equal(commit, commitStr);
    query.where(builder.and(n1, n2, n3));
    query.orderBy(builder.asc(root.get("id")));
    return getAll(query);
  }
  
  /**
   * Gets source code edits.
   * @return source code edits.
   */
  public List<Edit> getSrcEditsGreatherThan(final Long editId, final int limit) {
    final CriteriaBuilder builder = em.getCriteriaBuilder();
    final CriteriaQuery<Edit> query = builder.createQuery(Edit.class);
    final Root<Edit> root = query.from(Edit.class);
    final Expression<Edit> context = root.get("context");
    final Expression<Edit> dst = root.get("dst");
    final Expression<Long> id = root.get("id");
    final Predicate n1 = builder.isNotNull(context);
    final Predicate n2 = builder.isNotNull(dst);
    final Predicate n3 = builder.greaterThan(id, editId);
    query.where(builder.and(n1, n2, n3));
    query.orderBy(builder.asc(id));
    final List<Edit> list = em.createQuery(query).setMaxResults(limit).getResultList();
    return list;
  }
  
  /**
   * Gets source code edits.
   * @return source code edits.
   */
  public List<Edit> getSrcEditsByDcap(final String dcap, final int d) {
    final CriteriaBuilder builder = em.getCriteriaBuilder();
    final CriteriaQuery<Edit> query = builder.createQuery(Edit.class);
    final Root<Edit> root = query.from(Edit.class);
    final Expression<Edit> dst = root.get("dst");
    final Expression<Edit> dcapColumn = root.get("dcap" + d);
    final Predicate n1 = builder.equal(dcapColumn, dcap);
    final Predicate n2 = builder.isNotNull(dst);
    query.where(builder.and(n1, n2));
    return getAll(query);
  }
  
  /**
   * Gets source code edits.
   * @return source code edits.
   */
  public List<Edit> getSrcEditsByDcap(final String srcDcap, final String dstDcap, final int d) {
    final String query = "SELECT e FROM Edit e JOIN fetch e.dst d"
        + " WHERE e.dst IS NOT NULL"
        + " AND e.dcap" + d + " = '" + srcDcap + "'"
        + " AND d.dcap" + d + " = '" + dstDcap + "'"
        + " ORDER BY e.id";
    final List<Edit> list = em.createQuery(query, Edit.class).getResultList();
    return list;
  }
  
  /**
   * Gets source code edits.
   * @return source code edits.
   */
  public List<String> getAllDcaps(final int d) {
    final String query = "SELECT DISTINCT(e.dcap" + d + ") FROM Edit e WHERE e.dcap" + d + " IS NOT NULL";
    final List<String> list = em.createQuery(query, String.class).getResultList();
    return list;
  }
  
  /**
   * Gets source code edits.
   * @return source code edits.
   */
  public List<String> getAllCommits(final String project) {
    final String query = "select e.commit FROM Edit e WHERE e.dst IS NOT NULL AND e.context IS NOT NULL"
        + " AND e.project like '%" + project + "_old%'"
        + " ORDER BY e.id asc";
    final List<String> list = em.createQuery(query, String.class).getResultList();
    return list;
  }
  
  /**
   * Gets last edit
   * @return source code edits.
   */
  public Edit getLastEdit() {
    final String query = "select e FROM Edit e where e.dst IS NOT NULL and e.context IS NOT NULL"
                      + " ORDER BY e.id desc";
    final List<Edit> list = em.createQuery(query, Edit.class).setMaxResults(1).getResultList();
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
  public List<Import> getImports(final Edit edit) {
    final String query = "SELECT i FROM Edit e JOIN fetch e.imports i "
                         + "WHERE e.id = " + edit.getId() + ""
                         + " ORDER BY i.id";
    final List<Import> list = em.createQuery(query, Import.class).getResultList();
    return list;
  }
}
