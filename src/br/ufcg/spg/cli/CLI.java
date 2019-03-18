package br.ufcg.spg.cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CLI {

  public static List<String> runCommandLine(String cmd) {
    List<String> lines = new ArrayList<>();
    System.out.println("Execute shell commands example");
    System.out.println();
    try {
      System.out.println("Executing command: " + cmd);
      Process p = Runtime.getRuntime().exec(cmd);
      System.out.println();
      System.out.println("Result:");
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line = null;
      while ((line = reader.readLine()) != null) {
        //System.out.println(line);
        lines.add(line);
      }
      int result = p.waitFor();
      if (result == 0) {
        StringBuilder err = new StringBuilder();
        reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while ((line = reader.readLine()) != null) {
          err.append(line).append('\n');
        }
        throw new RuntimeException(err.toString());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return lines;
  }
}
