package br.ufcg.spg.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CLI {

  public static List<String> runCommandLine(String cmd) {
    List<String> lines = new ArrayList<>();
    try {
      Process p = Runtime.getRuntime().exec(cmd, null);
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        //System.out.println(line);
        lines.add(line);
      }
      p.waitFor();
      StringBuilder err = new StringBuilder();
      reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      while ((line = reader.readLine()) != null) {
        //System.out.println(line);
        err.append(line).append('\n');
        lines.add(line);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return lines;
  }
}
