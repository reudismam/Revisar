package br.ufcg.spg.transformation;

import br.ufcg.spg.bean.Tuple;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InnerClassUtils {
  public static Tuple<String, String> getInnerClassImport(ASTNode importStm) {
    return getInnerClassImport(importStm.toString());
  }

  public static Tuple<String, String> getInnerClassImport(String importStm) {
    String pattern = "([A-Z][A-Za-z]*)\\.([A-Z][A-Za-z]*)";
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(importStm);
    if (m.find()) {
      if (!importStm.contains("java.util")) {
        return new Tuple<>(m.group(1), m.group(2));
      }
    }
    return null;
  }
}
