package br.ufcg.spg.checker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompositeCodeChecker extends CodeChecker {

  private List<CodeChecker> checkerList = new ArrayList<>();

  public CompositeCodeChecker() {
    checkerList.add(new CheckStyleCodeChecker());
    checkerList.add(new PMDCodeChecker());
  }

  public List<CheckerReport> getCheckerReports(String fileToAnalyze) {
    List<CheckerReport>  reports = new ArrayList<>();
    for (CodeChecker checker : checkerList) {
      List<CheckerReport> checkerReports = checker.getCheckerReports(fileToAnalyze);
      reports.addAll(checkerReports);
    }
    return reports;
  }

  public Map<Integer, List<CheckerReport>> getCheckerReportsByLine(String fileToAnalyze) {
    Map<Integer, List<CheckerReport>> map = new HashMap<>();
    for (CodeChecker checker : checkerList) {
      Map<Integer, List<CheckerReport>> checkerMap = checker.getCheckerReportsByLine(fileToAnalyze);
      for (Map.Entry<Integer, List<CheckerReport>> entry : checkerMap.entrySet()) {
        if (map.containsKey(entry.getKey())) {
          map.get(entry.getKey()).addAll(entry.getValue());
        }
        else {
          map.put(entry.getKey(), entry.getValue());
        }
      }
    }
    return map;
  }

  @Override
  CheckerReport getCheckerReport(String line) {
    throw new RuntimeException("Not aplicable!!");
  }

  @Override
  String getCommandLine(String fileToAnalyze) {
    throw new RuntimeException("Not aplicable!!");
  }
}
