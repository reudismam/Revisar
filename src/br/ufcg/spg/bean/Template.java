package br.ufcg.spg.bean;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public class Template {
  private ASTNode       template;
  private List<ASTNode> variables;
  private List<ASTNode> holes;

  public Template() {
  }

  /**
   * Constructor.
   */
  public Template(ASTNode template, List<ASTNode> variables, List<ASTNode> holes) {
    this.template = template;
    this.variables = variables;
    this.holes = holes;
  }

  public ASTNode getTemplate() {
    return template;
  }

  public void setTemplate(ASTNode template) {
    this.template = template;
  }

  public List<ASTNode> getVariables() {
    return variables;
  }

  public void setVariables(List<ASTNode> variables) {
    this.variables = variables;
  }
  
  public List<ASTNode> getHoles() {
    return holes;
  }
  
  public void setHoles(List<ASTNode> holes) {
    this.holes = holes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((template == null) ? 0 : template.hashCode());
    result = prime * result + ((variables == null) ? 0 : variables.hashCode());
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
    Template other = (Template) obj;
    if (template == null) {
      if (other.template != null) {
        return false;
      }
    } else if (!template.equals(other.template)) {
      return false;
    }
    if (variables == null) {
      if (other.variables != null) {
        return false;
      }
    } else if (!variables.equals(other.variables)) {
      return false;
    }
    return true;
  }
}
