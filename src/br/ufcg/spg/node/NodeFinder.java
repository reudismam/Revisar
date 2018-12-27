package br.ufcg.spg.node;

import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.PositionNodeMatcher;
import br.ufcg.spg.matcher.PositionTreeMatcher;
import br.ufcg.spg.matcher.ValueNodeMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.NodeMatchCalculator;
import br.ufcg.spg.matcher.calculator.TreeMatchCalculator;

import com.github.gumtreediff.tree.ITree;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class NodeFinder {

  /**
   * Get Node.
   */
  public static ASTNode getNode(final CompilationUnit cunit, final ITree dstTree,
      final ITree dstMatch) {
    IMatcher<ITree> match;
    MatchCalculator<ITree> mcalc;
    match = new PositionTreeMatcher(dstMatch);
    mcalc = new TreeMatchCalculator(match);
    final ITree dstTarget = mcalc.getNode(dstTree);
    IMatcher<ASTNode> nodematch = new PositionNodeMatcher(dstTarget);
    MatchCalculator<ASTNode> nodecalc = new NodeMatchCalculator(nodematch);
    final ASTNode dstAstNode = nodecalc.getNode(cunit);
    return dstAstNode;
  }

  /**
   * Get nodes with same name.
   * @param target target where the nodes will be searched for.
   * @param nodeContent content of the node to be searched for.
   */
  public static List<ASTNode> getNodesSameName(final ASTNode target, final String nodeContent) {
    final IMatcher<ASTNode> match = new ValueNodeMatcher(nodeContent);
    final MatchCalculator<ASTNode> mcal = new NodeMatchCalculator(match);
    return mcal.getNodes(target);
  }

}
