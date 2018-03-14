package br.ufcg.spg.refaster;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class NoneBodyConfig implements IConfigBody {
  private final MethodDeclaration method;

  /**
   * Constructor.
   */
  public NoneBodyConfig(final MethodDeclaration method) {
    super();
    this.method = method;
  }

  @Override
  public MethodDeclaration config() {
    System.out.println("DEBUG: COULD NOT DETERMINE RETURN TYPE.");
    return method;
  }
}
