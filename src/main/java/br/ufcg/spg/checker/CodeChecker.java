package br.ufcg.spg.checker;

import br.ufcg.spg.cli.CLI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CodeChecker {

  public List<CheckerReport> getCheckerReports(String fileToAnalyze) {
    String cmd = getCommandLine(fileToAnalyze);
    List<String> result = CLI.runCommandLine(cmd);
    List<CheckerReport> reports = new ArrayList<>();
    for (String str : result) {
      CheckerReport report = getCheckerReport(str);
      if (report != null) {
        reports.add(report);
      }
    }
    return reports;
  }

  public Map<Integer, List<CheckerReport>> getCheckerReportsByLine(String fileToAnalyze) {
    String cmd = getCommandLine(fileToAnalyze);
    List<String> result = CLI.runCommandLine(cmd);
    Map<Integer, List<CheckerReport>> reports = new HashMap<>();
    for (String str : result) {
      CheckerReport report = getCheckerReport(str);
      if (report != null) {
        if (!reports.containsKey(report.getLine())) {
          reports.put(report.getLine(), new ArrayList<>());
        }
        reports.get(report.getLine()).add(report);
      }
    }
    return reports;
  }

  abstract CheckerReport getCheckerReport(String str);

  abstract String getCommandLine(String fileToAnalyze);
}

