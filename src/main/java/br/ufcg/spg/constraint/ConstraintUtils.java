package br.ufcg.spg.constraint;

import br.ufcg.spg.matcher.AllNodeMatcher;
import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.NodeMatchCalculator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ConstraintUtils {

  /**
   * gets the constraints associated with the provided file.
   * 
   * @param unit
   *          - provided file
   * @return constraints associated with the provided file.
   */
  public static List<ASTNode> constraints(final CompilationUnit unit) {
    final IMatcher<ASTNode> matcher = new AllNodeMatcher();
    final MatchCalculator<ASTNode> mcal = new NodeMatchCalculator(matcher);
    final List<ASTNode> nodes = mcal.getNodes(unit);
    final List<ASTNode> errors = new ArrayList<ASTNode>();
    for (final ASTNode node : nodes) {
      final List<IConstraintRule> rules = ConstraintFactory.getConstraints(node);
      for (final IConstraintRule rule : rules) {
        if (!rule.isApplicableTo(node)) {
          continue;
        }
        if (!rule.isValid(node)) {
          errors.add(node);
        }
      }
    }
    return errors;
  }
}
