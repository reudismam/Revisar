package br.ufcg.spg.refaster;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public interface IConfigBody {
  /**
   * Configures body.
   * @return configured method
   */
  public MethodDeclaration config();
}
