package br.ufcg.spg.parser;

import br.ufcg.spg.project.ProjectAnalyzer;
import br.ufcg.spg.project.Version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;

public class JParser {

  private Document document;

  public Document getDocument() {
    return document;
  }

  public void setDocument(final Document document) {
    this.document = document;
  }

  /**
   * Parses java files.
   * 
   * @param file
   *          - file
   * @return parsed file
   */
  public CompilationUnit parseWithDocument(final String file) {
    try {
      final String str = new String(Files.readAllBytes(Paths.get(file)));
      document = new Document(str);
      final ASTParser parser = ASTParser.newParser(AST.JLS8);
      parser.setSource(document.get().toCharArray());
      parser.setResolveBindings(true);
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setBindingsRecovery(true);
      parser.setStatementsRecovery(true);
      final Map<?,?> options = JavaCore.getOptions();
      parser.setCompilerOptions(options);
      final String unitName = file;
      parser.setUnitName(unitName);
      final String[] sources = { "\\" };
      final String [] classpath = ProjectAnalyzer.classpath(file);
      parser.setEnvironment(classpath, sources, new String[] { "UTF-8" }, true);
      parser.setSource(str.toCharArray());
      final CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
      return compilationUnit;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parses java files.
   * 
   * @param file
   *          - file
   * @return parsed file
   */
  public CompilationUnit parseWithDocument(final String file, final String[] sources, 
      final String[] classpath) {
    try {
      final String str = new String(Files.readAllBytes(Paths.get(file)));
      document = new Document(str);
      final ASTParser parser = ASTParser.newParser(AST.JLS8);
      parser.setSource(document.get().toCharArray());
      parser.setResolveBindings(true);
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setBindingsRecovery(true);
      parser.setStatementsRecovery(true);
      final Map<?, ?> options = JavaCore.getOptions();
      parser.setCompilerOptions(options);
      final String unitName = file;
      parser.setUnitName(unitName);
      parser.setEnvironment(classpath, sources, new String[] { "UTF-8" }, true);
      parser.setSource(str.toCharArray());
      final CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
      return compilationUnit;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parses java files.
   * @param file file
   * @return parsed file
   */
  public static CompilationUnit parse(final String file) throws IOException {
    final String str = new String(Files.readAllBytes(Paths.get(file)));
    final ASTParser parser = ASTParser.newParser(AST.JLS8);
    parser.setResolveBindings(true);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setBindingsRecovery(true);
    parser.setStatementsRecovery(true);
    final Map<?, ?> options = JavaCore.getOptions();
    parser.setCompilerOptions(options);
    final String unitName = file;
    parser.setUnitName(unitName);
    final String[] sources = { "\\" };
    final String[] classpath = ProjectAnalyzer.classpath(null);
    parser.setEnvironment(classpath, sources, new String[] { "UTF-8" }, true);
    parser.setSource(str.toCharArray());
    final CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
    return compilationUnit;
  }
  
  /**
   * Parses java files.
   * @param file file
   * @return parsed file
   */
  public static CompilationUnit parse(final String file, String source) throws IOException {
    final String str = source;
    final ASTParser parser = ASTParser.newParser(AST.JLS8);
    parser.setResolveBindings(true);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setBindingsRecovery(true);
    parser.setStatementsRecovery(true);
    final Map<String, String> options = JavaCore.getOptions();
    parser.setCompilerOptions(options);
    final String unitName = file;
    parser.setUnitName(unitName);
    final String[] sources = { "\\" };
    final String[] classpath = ProjectAnalyzer.classpath(null);
    parser.setEnvironment(classpath, sources, new String[] { "UTF-8" }, true);
    parser.setSource(str.toCharArray());
    final CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
    IProblem[] problems = compilationUnit.getProblems();
    if (problems != null && problems.length > 0) {
      System.out.println("Got " + problems.length  + " problems compiling the source file: ");
      for (IProblem problem : problems) {
        System.out.println(problem);
      }
    }
    return compilationUnit;
  }

  /**
   * Parses java file.
   * @param file file
   */
  public static CompilationUnit parseFromFile(final String file) throws IOException {
    final String source = FileUtils.readFileToString(new File(file));
    return parse(file, source);
  }

  /**
   * Parses java files.
   * 
   * @param file
   *          - file
   * @return parsed file
   */
  public static CompilationUnit parse(final String file, final Version version) 
      throws IOException {
    final String str = new String(Files.readAllBytes(Paths.get(file)));
    final ASTParser parser = ASTParser.newParser(AST.JLS8);
    parser.setResolveBindings(true);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setBindingsRecovery(true);
    parser.setStatementsRecovery(true);
    final Map<?, ?> options = JavaCore.getOptions();
    parser.setCompilerOptions(options);
    final String unitName = file;
    parser.setUnitName(unitName);
    final String [] sources = version.getSource();
    final String [] classpath = version.getClasspath();
    parser.setEnvironment(classpath, sources, new String[] { "UTF-8" }, true);
    parser.setSource(str.toCharArray());
    final CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
    return compilationUnit;
  }
}
