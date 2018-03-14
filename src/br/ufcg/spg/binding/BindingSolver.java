package br.ufcg.spg.binding;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class BindingSolver {
  
  /**
   * Gets type binding.
   * @param astNode - node to be analyzed
   * @return type binding
   */
  public static ITypeBinding typeBinding(final ASTNode astNode) {
    if (astNode instanceof Expression) {
      final Expression expr = (Expression) astNode;
      final ITypeBinding typeBinding = expr.resolveTypeBinding();
      return typeBinding;
    }
    return null;
  }
  
  /**
   * Gets the qualified name of the AST node.
   * @param astNode - node to be analyzed
   * @return qualified name of the AST node
   */
  public static String qualifiedName(final ASTNode astNode) {
    final ITypeBinding binding = typeBinding(astNode);
    if (binding != null) {
      return binding.getQualifiedName();
    }
    return null;
  }
}
