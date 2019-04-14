package br.ufcg.spg.checker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckStyleCodeChecker extends CodeChecker {

  @Override
  public CheckerReport getCheckerReport(String line) {
    final Pattern pattern = Pattern.compile("(\\[[A-Z]+\\]) (.+java):([0-9]+[0-9:]*) (.+) (\\[[a-zA-Z]+\\])");
    Matcher matcher = pattern.matcher(line);
    if (matcher.find()) {
      String kind = matcher.group(1);
      String file = matcher.group(2);
      String location = matcher.group(3);
      int lineNumber = Integer.parseInt(location.substring(0, location.indexOf(':')));
      String message = matcher.group(4);
      String classification = matcher.group(5);
      CheckerReport report = new CheckerReport(kind, file, lineNumber, message, classification);
      return report;
    }
    return null;
  }

  @Override
  public String getCommandLine(String fileToAnalyze){
    String cmd = "java -jar checkstyle-8.18-all.jar -c /sun_checks.xml " + fileToAnalyze;
    return cmd;
  }
}

