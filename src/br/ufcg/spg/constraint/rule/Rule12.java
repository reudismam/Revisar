package br.ufcg.spg.constraint.rule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class Rule12 extends RuleBase {

  @Override
  public boolean isValidRule(final ASTNode node) {
    final TypeDeclaration typeDcl = RuleUtils.getTypeDeclaration(node);
    final FieldDeclaration[] fields = typeDcl.getFields();
    final Set<String> map = new HashSet<>();
    for (final FieldDeclaration field : fields) {
      final List<?> list = field.fragments();
      for (final Object obj : list) {
        final VariableDeclarationFragment frag = (VariableDeclarationFragment) obj;
        final String name = frag.getName().toString();
        if (map.contains(name)) {
          return false;
        }
        map.add(name);
      }
    }
    return true;
  }

  @Override
  public boolean isApplicableTo(final ASTNode node) {
    return node instanceof FieldDeclaration;
  }
}
