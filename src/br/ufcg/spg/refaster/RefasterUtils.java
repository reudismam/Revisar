package br.ufcg.spg.refaster;

import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;

import br.ufcg.spg.diff.DiffCalculator;
import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.PositionTreeMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.TreeMatchCalculator;
import br.ufcg.spg.refaster.config.ReturnStatementConfig;

public class RefasterUtils {

  static ITree getMatch(final ReturnStatementConfig rconfig, final DiffCalculator diff) {
    final ITree srcTree = diff.getSrc().getRoot();
    IMatcher<ITree> match = new PositionTreeMatcher(rconfig.getTarget());
    MatchCalculator<ITree> mcalc = new TreeMatchCalculator(match);
    final ITree srcTarget = mcalc.getNode(srcTree);
    final Matcher matcher = diff.getMatcher();
    final ITree dstMatch = matcher.getMappings().getDst(srcTarget);
    return dstMatch;
  }

}
