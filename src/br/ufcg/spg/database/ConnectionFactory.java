package br.ufcg.spg.database;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class ConnectionFactory {
  private static ConnectionFactory instance;
  private EntityManager em;
  
  private ConnectionFactory() {
    final EntityManagerFactory factory = Persistence.createEntityManagerFactory("AntiUnification");
    em = factory.createEntityManager();
  }
  
  /**
   * Gets singleton instance.
   * @return singleton instance
   */
  public static synchronized ConnectionFactory getInstance() {
    if (instance == null) {
      instance = new ConnectionFactory();
    }
    return instance;
  }
  
  /**
   * Gets connection.
   * @return Database connection
   */
  public EntityManager getConnection() {
    return em;
  }
}
