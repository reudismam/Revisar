package br.ufcg.spg.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PMDUtils {

  public static void main(String ... args) {
    List<CheckStyleReport> reports = getPMDReports("temp1.java");
    for (CheckStyleReport report : reports) {
      System.out.println(report);
    }
  }

  public static List<CheckStyleReport> getPMDReports(String fileToAnalyze) {
    String cmd = "pmd.bat -d " + fileToAnalyze + " -f text -R category/java/codestyle.xml";
    List<String> result = CLI.runCommandLine(cmd);
    List<CheckStyleReport> reports = new ArrayList<>();
    for (String str : result) {
      CheckStyleReport report = PMDUtils.getPMDReport(str);
      if (report != null) {
        reports.add(report);
      }
    }
    return reports;
  }

  public static Map<Integer, List<CheckStyleReport>> getPMDReportByLine(String fileToAnalyze) {
    String cmd = "java -jar checkstyle-8.18-all.jar -c /sun_checks.xml " + fileToAnalyze;
    List<String> result = CLI.runCommandLine(cmd);
    Map<Integer, List<CheckStyleReport>> reports = new HashMap<>();
    for (String str : result) {
      CheckStyleReport report = PMDUtils.getPMDReport(str);
      if (report != null) {
        if (!reports.containsKey(report.getLine())) {
          reports.put(report.getLine(), new ArrayList<>());
        }
        reports.get(report.getLine()).add(report);
      }
    }
    return reports;
  }

  private static CheckStyleReport getPMDReport(String line) {
    final Pattern pattern = Pattern.compile("(.+java):([0-9]+): *(.+)");
    Matcher matcher = pattern.matcher(line);
    if (matcher.find()) {
      String file = matcher.group(1);
      int lineNumber = Integer.parseInt(matcher.group(2));
      String message = matcher.group(3).trim();
      String classification = "Code Style";
      String kind = classification;
      CheckStyleReport report = new CheckStyleReport(kind, file, lineNumber, message, classification);
      return report;
    }
    return null;
  }
}
