package br.ufcg.spg.compile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

public class JCompiler {
  
  /**
   * gets compilation errors.
   * 
   * @param srcFilePath source path
   * @return compilation errors
   */
  public static List<br.ufcg.spg.bean.Error> compileFiles(String srcFilePath) throws IOException {
    CompilationProgress progress = new EJCCompilationProgress();
    PrintWriter output = new PrintWriter(System.out);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    PrintWriter error = new PrintWriter(ps);
    String classPath = System.getProperty("java.class.path");
    BatchCompiler.compile(
        "-d build -cp " + classPath
            + ";C:\\Users\\SPG-04\\workspace\\Projects\\ant_old\\lib\\commons-net-1.4.1.jar;C:\\"
            + "Users\\SPG-04\\workspace\\Projects\\ant_old\\lib\\regexp-1.3.jar;C:\\Users\\"
            + "SPG-04\\workspace\\Projects\\ant_old\\lib\\oro-2.0.8.jar"
            + " @C:\\Users\\SPG-04\\workspace\\Projects\\ant_old\\sources.txt -target 1.8 -1.8",
        output, error, progress);
    String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
    // Patterns
    String warningPattern = "[0-9]+\\. ERROR in ";
    String idPattern = "[0-9]+\\.";
    String filePattern = "ERROR in .+\\.java";
    String linePattern = "at line [0-9]+";
    String positionPattern = "\\^+";
    Pattern warningPatternCompilation = Pattern.compile(warningPattern);
    Pattern idPatternCompilation = Pattern.compile(idPattern);
    Pattern filePatternCompilation = Pattern.compile(filePattern);
    Pattern linePatternCompilation = Pattern.compile(linePattern);
    Pattern positionPatternCompilation = Pattern.compile(positionPattern);
    // Error messages line by line
    String[] separated = content.split("\n");
    FileUtils.writeStringToFile(new File("C:\\Users\\SPG-04\\workspace\\content.txt"), content);
    int i = 0;
    List<br.ufcg.spg.bean.Error> errors = new ArrayList<br.ufcg.spg.bean.Error>();
    while (i < separated.length) {
      java.util.regex.Matcher m = warningPatternCompilation.matcher(separated[i]);
      if (m.find()) {
        String errorLine = separated[i++];
        // gets the id of the bug
        m = idPatternCompilation.matcher(errorLine);
        m.find();
        String group = m.group(0);
        int id = Integer.parseInt(group.substring(0, group.length() - 1));
        // gets the file of the bug
        m = filePatternCompilation.matcher(errorLine);
        m.find();
        String file = m.group(0).substring(9);
        // gets line number
        m = linePatternCompilation.matcher(errorLine);
        m.find();
        int line = Integer.parseInt(m.group().substring(8));
        // gets the source code line
        String code = "";
        String lineCode = separated[i++];
        while (!positionPatternCompilation.matcher(lineCode).find()) {
          code += lineCode;
          lineCode = separated[i++];
        }
        String positionLine = lineCode;
        m = positionPatternCompilation.matcher(positionLine);
        m.find();
        // gets the start position
        int startPosition = m.start();
        // gets the end position
        int endPosition = m.end();
        if (endPosition <= code.length()) {
          System.err.println("Node: " + code.substring(startPosition, endPosition));
        } else {
          System.err.println("Node: " + code.substring(startPosition, code.length()));
        }
        String description = separated[i];
        br.ufcg.spg.bean.Error er = new br.ufcg.spg.bean.Error(
            id, line, file, code, startPosition, endPosition, description);
        errors.add(er);
        System.err.print(errorLine);
        System.err.print(code);
        System.err.print(positionLine);
        System.err.print(description);
      }
      i++;
    }
    return errors;
  }
}
