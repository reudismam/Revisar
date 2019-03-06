package br.ufcg.spg.edit;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.diff.DiffCalculator;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.project.Version;

import com.github.gumtreediff.tree.TreeContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;


public class EditUtils {

  /**
   * Computes before and after version of the class.
   * @param file - source file path
   * @param content - destination code
   * @return before and after version of the class
   */
  public static CompilationUnit cunitForContent2(
      final String file, final String content, final Version version) {
    try {
      final String src = new String(Files.readAllBytes(Paths.get(file)));
      FileUtils.writeStringToFile(new File(file), content);
      final CompilationUnit dstComp = JParser.parse(file, version);
      FileUtils.writeStringToFile(new File(file), src);
      return dstComp;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Computes before and after version of the class.
   * @param srcFile - source file path
   * @param dst - destination code
   * @return before and after version of the class
   */
  public static Tuple<CompilationUnit, CompilationUnit> beforeAfter(
      final String srcFile, final String dst) {
    try {
      final String src = new String(Files.readAllBytes(Paths.get(srcFile)));
      final CompilationUnit srcComp = JParser.parse("temp1.java", src);
      FileUtils.writeStringToFile(new File(srcFile), dst);
      final CompilationUnit dstComp = JParser.parse("temp2.java", dst);
      FileUtils.writeStringToFile(new File(srcFile), src);
      final Tuple<CompilationUnit, CompilationUnit> t = new Tuple<>(srcComp, dstComp);
      return t;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Computes before and after version of the class.
   * @param srcFile - source file path
   * @param dst - destination code
   * @return before and after version of the class
   */
  public static Tuple<TreeContext, TreeContext> beforeAfterCxt(
      final String srcFile, final String dst) {
    try {
      final String src = new String(Files.readAllBytes(Paths.get(srcFile)));
      final TreeContext srcCont = DiffCalculator.generator(srcFile);
      FileUtils.writeStringToFile(new File(srcFile), dst);
      final TreeContext dstCont = DiffCalculator.generator(srcFile);
      FileUtils.writeStringToFile(new File(srcFile), src);
      final Tuple<TreeContext, TreeContext> t = new Tuple<>(srcCont, dstCont);
      return t;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
