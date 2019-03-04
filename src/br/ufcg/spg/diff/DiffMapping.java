package br.ufcg.spg.diff;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.MappingStore;

import java.util.List;

/**
 * Diff based on mapping.
 */
public class DiffMapping extends DiffCalculator {
  /**
   * Mapping information.
   */
  private final MappingStore mapping;
  
  /**
   * Constructor.
   * @param mapping mapping
   */
  public DiffMapping(final MappingStore mapping) {
    super();
    this.mapping = mapping;
  }
  
  /**
   * Calculates diff.
   * @return diff
   */
  @Override
  public List<Action> computeDiff() {
    try {
      final ActionGenerator generator = new ActionGenerator(src.getRoot(), dst.getRoot(), mapping);
      generator.generate();
      return generator.getActions(); // return the actions
    } catch (final UnsupportedOperationException e) {
      throw new RuntimeException(e);
    }
  }
}
