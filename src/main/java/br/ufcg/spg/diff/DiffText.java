package br.ufcg.spg.diff;

import com.github.gumtreediff.actions.model.Action;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * Diff based on text.
 */
public class DiffText extends DiffCalculator {
  
  /**
   * Source code.
   */
  private final transient String srcSource;
  
  /**
   * Destination code.
   */
  private final transient String dstSource;
  
  /**
   * Constructor.
   * @param srcSource source code
   * @param dstSource destination code
   */
  public DiffText(final String srcSource, final String dstSource) {
    super();
    this.srcSource = srcSource;
    this.dstSource = dstSource;
  }

  /**
   * Computes diff.
   * @return diff
   */
  @Override
  public List<Action> computeDiff() {
    try {
      final String srcFile = "temp1.java";
      final String dstFile = "temp2.java";
      FileUtils.writeStringToFile(new File(srcFile), srcSource);
      FileUtils.writeStringToFile(new File(dstFile), dstSource);
      final DiffCalculator diff = new DiffPath(srcFile, dstFile);
      final List<Action> actions =  diff.diff();
      diff.src = src;
      diff.dst = dst;
      return actions;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
