package br.ufcg.spg.diff;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.TreeContext;

import java.util.List;

public class DiffTreeContext extends DiffCalculator {
 
  /**
   * Constructor.
   * @param src source code
   * @param dst destination code
   */
  public DiffTreeContext(final TreeContext src, final TreeContext dst) {
    super();
    this.src = src;
    this.dst = dst;
  }

  /**
   * Computes diff.
   * @return diff
   */
  @Override
  public List<Action> computeDiff() {
    try {
      matcher = Matchers.getInstance().getMatcher(src.getRoot(), dst.getRoot());
      matcher.match();
      final ActionGenerator g = new ActionGenerator(
          src.getRoot(), dst.getRoot(), matcher.getMappings());
      g.generate();
      final List<Action> actions = g.getActions(); // return the actions
      return actions;
    } catch (final UnsupportedOperationException e) {
      throw new RuntimeException(e);
    }
  }
}
