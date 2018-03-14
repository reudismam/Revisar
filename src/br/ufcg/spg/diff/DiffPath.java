package br.ufcg.spg.diff;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.matchers.Matchers;

import java.io.IOException;
import java.util.List;

/**
 * Calculates diff based on file paths.
 */
public class DiffPath extends DiffCalculator {
  
  /**
   * Source path.
   */
  private final transient String srcFilePath;
  
  /**
   * Destination path.
   */
  private final transient String dstFilePath;

  /**
   * Constructor.
   * 
   * @param srcFilePath source file path
   * @param dstFilePath destination file path
   */
  public DiffPath(final String srcFilePath, final String dstFilePath) {
    super();
    this.srcFilePath = srcFilePath;
    this.dstFilePath = dstFilePath;
  }

  /**
   * Computes diff.
   * @return diff
   */
  @Override
  public List<Action> computeDiff() {
    Run.initGenerators();
    try {
      src = Generators.getInstance().getTree(srcFilePath);
      dst = Generators.getInstance().getTree(dstFilePath);
      matcher = Matchers.getInstance().getMatcher(src.getRoot(), dst.getRoot());
      matcher.match();
      final ActionGenerator generator = new ActionGenerator(src.getRoot(), dst.getRoot(), 
          matcher.getMappings());
      generator.generate();
      final List<Action> actions = generator.getActions(); // return the actions
      return actions;
    } catch (UnsupportedOperationException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}
