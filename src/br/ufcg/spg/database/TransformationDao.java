package br.ufcg.spg.database;

import java.util.List;

import br.ufcg.spg.transformation.Transformation;

public class TransformationDao extends GenericDao<Transformation, Long> {
  
  private static TransformationDao instance;
  
  private TransformationDao() {
    super(Transformation.class);
  }  
  
  /**
   * Gets singleton instance.
   * @return singleton instance.
   */
  public static TransformationDao getInstance() {
    if (instance == null) {
      instance = new TransformationDao();
    }
    return instance;
  }
  
  /**
   * Gets source code edits.
   * @return source code edits.
   */
  public long getLastClusterId() {
    final String query = "select c.id from Transformation t JOIN fetch t.cluster c"
                      + " order by t.id desc";
    final List<Long> list = em.createQuery(query, Long.class).setMaxResults(1).getResultList();
    if (list.isEmpty()) {
      return -1;
    } else {
      return list.get(0);
    }
  }
}
