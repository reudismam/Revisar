package br.ufcg.spg.main;

public class MainArguments {
  private String projects;
  private static MainArguments instance;
  
  private MainArguments() {
  }
  
  /**
   * Singleton instance.
   */
  public static MainArguments getInstance() {
    if (instance == null) {
      instance = new MainArguments();
    }
    return instance;
  }

  public String getProjects() {
    return projects;
  }

  public void setProjects(String projects) {
    this.projects = projects;
  }
}
