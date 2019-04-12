package br.ufcg.spg.constraint.rule;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class Rule11 extends RuleBase {

  @Override
  public boolean isValidRule(final ASTNode node) {
    final MethodDeclaration methodDcl = (MethodDeclaration) node;
    final IMethodBinding methodType = methodDcl.resolveBinding();
    if (methodType == null) {
      return false;
    }
    final ITypeBinding typeBinding = methodType.getDeclaringClass();
    final Map<String, List<IMethodBinding>> map = new Hashtable<>();
    for (final IMethodBinding method : typeBinding.getDeclaredMethods()) {
      final String name = method.getName();
      if (!map.containsKey(name)) {
        map.put(name, new ArrayList<IMethodBinding>());
      }
      map.get(name).add(method);
    }
    for (final Entry<String, List<IMethodBinding>> entry : map.entrySet()) {
      final List<IMethodBinding> mbinds = entry.getValue();
      if (mbinds.size() > 1) {
        continue;
      }
      final IMethodBinding mbind = mbinds.get(0);
      final ITypeBinding [] typeParams0 = mbind.getParameterTypes();
      for (int i = 1; i < mbinds.size(); i++) {
        final ITypeBinding [] typeParamsi = mbinds.get(i).getParameterTypes();
        if (typeParams0.length != typeParamsi.length) {
          continue;
        }
        for (int j = 0; j < typeParams0.length; j++) {
          final ITypeBinding param0 = typeParams0[j];
          final ITypeBinding parami = typeParamsi[j];
          final String param0Str = param0.getQualifiedName();
          final String paramiStr = parami.getQualifiedName();
          if (!param0Str.equals(paramiStr)) {
            continue;
          }
        }
      }
    }
    return true;
  }

  @Override
  public boolean isApplicableTo(final ASTNode node) {
    return node instanceof MethodDeclaration;
  }
}
