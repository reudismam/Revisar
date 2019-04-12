package br.ufcg.spg.transformation;

import org.eclipse.jdt.core.dom.*;

public class NameUtils {

  public static String extractSimpleName(Type type) {
    String typeStr = type.toString();
    return extractSimpleName(typeStr);
  }

  public static String extractSimpleName(String typeStr) {
    if (typeStr.contains("<")) {
      typeStr = typeStr.substring(0, typeStr.indexOf("<"));
    }
    if (typeStr.contains(".")) {
      typeStr = typeStr.substring(typeStr.lastIndexOf(".") + 1);
    }
    if (typeStr.contains(";")) {
      typeStr = typeStr.replaceAll(";", "").trim();
    }
    return typeStr;
  }
}
