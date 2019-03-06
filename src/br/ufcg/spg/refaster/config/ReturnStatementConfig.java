package br.ufcg.spg.refaster.config;

import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.project.Version;
import br.ufcg.spg.replacement.Replacement;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ReturnStatementConfig {
  private String commit;
  private String path;
  private ASTNode target;
  private List<ASTNode> nodes;
  private List<Replacement<ASTNode>> targetList;
  private CompilationUnit refasterRule; 
  private MethodDeclaration method;
  //private Version version;
  private String pi;
  
  public String getCommit() {
    return commit;
  }

  public void setCommit(String commit) {
    this.commit = commit;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public ASTNode getTarget() {
    return target;
  }
  
  public void setTarget(ASTNode target) {
    this.target = target;
  }
  
  public List<ASTNode> getNodes() {
    return nodes;
  }
  
  public void setNodes(List<ASTNode> nodes) {
    this.nodes = nodes;
  }
  
  public List<Replacement<ASTNode>> getTargetList() {
    return targetList;
  }
  
  public void setTargetList(List<Replacement<ASTNode>> targetList) {
    this.targetList = targetList;
  }
  
  public CompilationUnit getRefasterRule() {
    return refasterRule;
  }
  
  public void setRefasterRule(CompilationUnit refasterRule) {
    this.refasterRule = refasterRule;
  }
  
  public MethodDeclaration getMethod() {
    return method;
  }
  
  public void setMethod(MethodDeclaration method) {
    this.method = method;
  }
  
  /*public Version getVersion() {
    return version;
  }
  
  public void setVersion(Version version) {
    this.version = version;
  }*/
  
  public String getPi() {
    return pi;
  }
  
  public void setPi(String pi) {
    this.pi = pi;
  }
}
