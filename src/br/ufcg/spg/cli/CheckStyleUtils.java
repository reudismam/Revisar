package br.ufcg.spg.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckStyleUtils {

  public static List<CheckStyleReport> getCheckyStyleReports(String fileToAnalyze) {
    String cmd = "java -jar checkstyle-8.18-all.jar -c /sun_checks.xml " + fileToAnalyze;
    List<String> result = CLI.runCommandLine(cmd);
    List<CheckStyleReport> reports = new ArrayList<>();
    for (String str : result) {
      CheckStyleReport report = CheckStyleUtils.getCheckstyleReport(str);
      if (report != null) {
        reports.add(report);
      }
    }
    return reports;
  }
  static CheckStyleReport getCheckstyleReport(String line) {
    final Pattern pattern = Pattern.compile("(\\[[A-Z]+\\]) (.+java):([0-9]+[0-9:]*) (.+) (\\[[a-zA-Z]+\\])");
    Matcher matcher = pattern.matcher(line);
    if (matcher.find()) {
      String kind = matcher.group(1);
      String file = matcher.group(2);
      String location = matcher.group(3);
      int lineNumber = Integer.parseInt(location.substring(0, location.indexOf(':')));
      String message = matcher.group(4);
      String classification = matcher.group(5);
      CheckStyleReport report = new CheckStyleReport(kind, file, lineNumber, message, classification);
      return report;
    }
    else {
      //System.out.println(str);
    }
    return null;
  }
}
