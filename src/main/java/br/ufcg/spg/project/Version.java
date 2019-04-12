package br.ufcg.spg.project;

public class Version {
  private String project;
  private String [] source;
  private String [] classpath;
  
  public String getProject() {
    return project;
  }
  
  public void setProject(final String project) {
    this.project = project;
  }
  
  public String[] getSource() {
    return source;
  }
  
  public void setSource(final String[] source) {
    this.source = source;
  }
  
  public String[] getClasspath() {
    return classpath;
  }
  
  public void setClasspath(final String[] classpath) {
    this.classpath = classpath;
  }
}
