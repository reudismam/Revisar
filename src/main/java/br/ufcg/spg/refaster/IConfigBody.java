package br.ufcg.spg.refaster;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public interface IConfigBody {
  /**
   * Configures body.
   * @return configured method
   */
  public MethodDeclaration config();
  
  public MethodDeclaration configReturnType(ASTNode node, 
      CompilationUnit rule, MethodDeclaration method);
}
