package br.ufcg.spg.source;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class SourceUtils {
  /**
   * Verify if we have a single line edit.
   * @param unit compilation unit
   * @param start start position of the node
   * @param end end position of the node
   * @return true if single line
   */
  public static boolean isSingleLine(final CompilationUnit unit, final int start, final int end) {
    final int lstart = getLineNumber(unit, start);
    final int lend = getLineNumber(unit, end);
    return lstart == lend;
  }
  
  private static int getLineNumber(final CompilationUnit unit, final int pos) {
    final int lnumber = unit.getLineNumber(pos);
    return lnumber;
  }
}
