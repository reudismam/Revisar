package br.ufcg.spg.evaluator.node;

import br.ufcg.spg.compile.CompilerUtils;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.matcher.AbstractMatchCalculator;
import br.ufcg.spg.matcher.PositionMatchCalculator;
import br.ufcg.spg.project.ProjectAnalyzer;
import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.project.Version;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Checks rules.
 */
public final class RuleNodeChecker implements INodeChecker {
  
  /**
   * List of rule each node must be checked in each node.
   */
  private final transient List<IValidationNodeRule> rules;

  /**
   * Edits to analyze.
   */
  private final transient List<Edit> srcEdits;

  /**
   * Constructor
   * 
   * @param depth
   *          depth on the AST tree.
   * @param rules
   *          rules to be analyzed.
   */
  private RuleNodeChecker(final List<IValidationNodeRule> rules, final List<Edit> srcEdits) {
    super();
    this.rules = rules;
    this.srcEdits = srcEdits;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean check() {
    try {
      final List<ASTNode> nodes = new ArrayList<>();
      for (final Edit srcEdit : srcEdits) {
        final ProjectInfo pi = ProjectAnalyzer.project(srcEdit);
        final Edit dstEdit = srcEdit.getDst();
        final String commit = dstEdit.getCommit();
        final Version srcVersion = pi.getSrcVersion();
        final CompilationUnit srcUnit = CompilerUtils.getCunit(srcEdit, commit, srcVersion, pi);
        final AbstractMatchCalculator mcalc = new PositionMatchCalculator(srcEdit.getStartPos(), 
            srcEdit.getEndPos());
        final ASTNode node = mcalc.getNode(srcUnit);
        nodes.add(node);
      }
      for (final IValidationNodeRule rule : rules) {
        if (!rule.checker(nodes)) {
          return false;
        }
      }
      return true;
    } catch (final Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a instance of a rule checker.
   * 
   * @param depth
   *          depth on the three to be analyzed.
   * @param srcEdits
   *          source code edits
   * @return a new instance of a rule checker.
   */
  public static RuleNodeChecker create(final int depth, final List<Edit> srcEdits) {
    final List<IValidationNodeRule> rules = new ArrayList<IValidationNodeRule>();
    final MethodInvocationNodeChecker minvo = new MethodInvocationNodeChecker();
    rules.add(minvo);
    return new RuleNodeChecker(rules, srcEdits);
  }
}
