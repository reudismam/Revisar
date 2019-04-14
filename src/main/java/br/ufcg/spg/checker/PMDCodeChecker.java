package br.ufcg.spg.checker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PMDCodeChecker extends CodeChecker {

  @Override
  CheckerReport getCheckerReport(String line) {
    final Pattern pattern = Pattern.compile("(.+java):([0-9]+): *(.+)");
    Matcher matcher = pattern.matcher(line);
    if (matcher.find()) {
      String file = matcher.group(1);
      int lineNumber = Integer.parseInt(matcher.group(2));
      String message = matcher.group(3).trim();
      String classification = "Code Style";
      String kind = classification;
      CheckerReport report = new CheckerReport(kind, file, lineNumber, message, classification);
      return report;
    }
    return null;
  }

  @Override
  String getCommandLine(String fileToAnalyze) {
    String cmd = "pmd.bat -d " + fileToAnalyze + " -f text -R category/java/codestyle.xml";
    return cmd;
  }
}
