package br.ufcg.spg.database;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class GenericDao<T, I extends Serializable> {
  
  private static final Logger logger = LogManager.getLogger(GenericDao.class.getName());
  
  protected EntityManager em;

  private Class<T> persistedClass;

  protected GenericDao() {
    em = ConnectionFactory.getInstance().getConnection();
  }

  protected GenericDao(final Class<T> persistedClass) {
    this();
    this.persistedClass = persistedClass;
  }

  /**
   * Save entity to database.
   * @param entity bean to be saved.
   */
  public T save(@Valid final T entity) {
    final EntityTransaction t = em.getTransaction();
    t.begin();
    em.persist(entity);
    em.flush();
    t.commit();
    return entity;
  }
  
  /**
   * Save entity to database.
   * @param entities beans to be saved.
   */
  public List<T> saveAll(@Valid final List<T> entities) {
    final EntityTransaction t = em.getTransaction();
    t.begin();
    for (int i = 0; i < entities.size(); i++) {
      final T entity = entities.get(i);
      logger.warn((((double)i) / entities.size()) * 100 + " % saved");
      em.persist(entity);
      if (i % 20 == 0) {
        em.flush(); 
        em.clear();
      }
    }
    t.commit();
    return entities;
  }
  
  /**
   * Update bean.
   * @param entity bean to be updated.
   */
  public T update(@Valid final T entity) {
    final EntityTransaction t = em.getTransaction();
    t.begin();
    em.merge(entity);
    em.flush();
    t.commit();
    return entity;
  }

  /**
   * Removes node by id.
   * @param id id
   */
  public void remove(final I id) {
    final T entity = find(id);
    final EntityTransaction tx = em.getTransaction();
    tx.begin();
    final T mergedEntity = em.merge(entity);
    em.remove(mergedEntity);
    em.flush();
    tx.commit();
  }

  /**
   * Gets all nodes.
   * @return all nodes
   */
  public List<T> getAll() {
    final CriteriaBuilder builder = em.getCriteriaBuilder();
    final CriteriaQuery<T> query = builder.createQuery(persistedClass);
    query.from(persistedClass);
    return em.createQuery(query).getResultList();
  }
  
  /**
   * Gets all nodes.
   * @return all nodes
   */
  public List<T> getAll(final CriteriaQuery<T> query) {
    return em.createQuery(query).getResultList();
  }

  public T find(final I id) {
    return em.find(persistedClass, id);
  }
}
