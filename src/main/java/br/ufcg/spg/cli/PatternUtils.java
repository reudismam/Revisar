package br.ufcg.spg.cli;

import br.ufcg.spg.checker.CheckerReport;
import br.ufcg.spg.checker.CodeChecker;
import br.ufcg.spg.checker.CompositeCodeChecker;
import br.ufcg.spg.checker.PMDCodeChecker;
import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.edit.Edit;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Map;

public class PatternUtils {

  public static PatternStatus getPatternStatus(Cluster srcCluster) {
    try {
      return getPatternStatus(srcCluster.getNodes().get(0));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  /**
   * Verifies whether an edit is new accordingly with checkstyle analyzer.
   * @param srcEdit source edit.
   * @return true if edit is new
   */
  private static PatternStatus getPatternStatus(Edit srcEdit) throws IOException {
    CodeChecker checker = new CompositeCodeChecker();
    Map<Integer, List<CheckerReport>> reports = checker.getCheckerReportsByLine("temp1.java");
    //Map<Integer, List<FindBug>>
    int offset = srcEdit.getStartPos();
    try (LineNumberReader r = new LineNumberReader(new FileReader("temp1.java"))) {
      int count = 0;
      while (r.read() != -1 && count < offset) {
        count++;
      }
      if (count != offset) {
        System.out.println("File is not long enough");
      }
      int line = r.getLineNumber() + 1;
      if (reports.containsKey(line)) {
          return PatternStatus.OLD;
      }

    }
    return PatternStatus.NEW;
  }
}
