package br.ufcg.spg.refaster.config;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.diff.DiffCalculator;
import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.replacement.Replacement;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class TransformationConfigObject {
  /**
   * Commit hash.
   */
  private String commit;
  /**
   * Path to file.
   */
  private String path;
  /** 
   * source node.
   */
  private ASTNode srcNode;
  /**
   * target node.
   */
  private ASTNode dstNode;
  /**
   * List of replacements from source code.
   */
  private List<Replacement<ASTNode>> srcList;
  /**
   * List of replacements from target code.
   */
  private List<Replacement<ASTNode>> dstList;
  /**
   * Current version of Refater rule.
   */
  private CompilationUnit refasterRule;
  /**
   * Before and after methods.
   */
  private Tuple<MethodDeclaration, MethodDeclaration> ba; 
  /**
   * Diff calculator.
   */
  private DiffCalculator diff;
  /**
   * Target compilation unit.
   */
  private CompilationUnit dstCu; 
  /**
   * Project information.
   */
  private ProjectInfo  pi;
  
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

  public ASTNode getNodeSrc() {
    return srcNode;
  }
  
  public void setNodeSrc(ASTNode srcNode) {
    this.srcNode = srcNode;
  }
  
  public ASTNode getNodeDst() {
    return dstNode;
  }
  
  public void setNodeDst(ASTNode dstNode) {
    this.dstNode = dstNode;
  }
  
  public List<Replacement<ASTNode>> getSrcList() {
    return srcList;
  }
  
  public void setSrcList(List<Replacement<ASTNode>> srcList) {
    this.srcList = srcList;
  }
  
  public List<Replacement<ASTNode>> getDstList() {
    return dstList;
  }
  
  public void setDstList(List<Replacement<ASTNode>> dstList) {
    this.dstList = dstList;
  }
  
  public CompilationUnit getRefasterRule() {
    return refasterRule;
  }
  
  public void setRefasterRule(CompilationUnit refasterRule) {
    this.refasterRule = refasterRule;
  }
  
  public Tuple<MethodDeclaration, MethodDeclaration> getBa() {
    return ba;
  }
  
  public void setBa(Tuple<MethodDeclaration, MethodDeclaration> ba) {
    this.ba = ba;
  }
  
  public DiffCalculator getDiff() {
    return diff;
  }
  
  public void setDiff(DiffCalculator diff) {
    this.diff = diff;
  }
  
  public CompilationUnit getDstCu() {
    return dstCu;
  }
  
  public void setDstCu(CompilationUnit dstCu) {
    this.dstCu = dstCu;
  }
  
  public ProjectInfo getPi() {
    return pi;
  }
  
  public void setPi(ProjectInfo pi) {
    this.pi = pi;
  }
}
