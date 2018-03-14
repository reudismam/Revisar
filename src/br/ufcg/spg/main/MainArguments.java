package br.ufcg.spg.main;

public class MainArguments {
  private String projects;
  private String projectFolder;
  
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

  public String getProjectFolder() {
    return projectFolder;
  }

  public void setProjectFolder(String projectFolder) {
    this.projectFolder = projectFolder;
  }
}
