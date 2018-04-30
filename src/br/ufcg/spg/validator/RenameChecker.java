package br.ufcg.spg.validator;

import at.jku.risc.stout.urauc.algo.AntiUnifyProblem.VariableWithHedges;
import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.cluster.UnifierCluster;
import br.ufcg.spg.replacement.ReplacementUtils;
import br.ufcg.spg.template.TemplateUtils;
import br.ufcg.spg.validator.node.INodeChecker;
import br.ufcg.spg.validator.template.ITemplateChecker;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RenameChecker implements INodeChecker, ITemplateChecker {
  private final Cluster srcCluster;
  private final Cluster dstCluster;
  
  /**
   * Constructor.
   * @param srcCluster source cluster
   * @param dstCluster destination cluster
   */
  public RenameChecker(final Cluster srcCluster, final Cluster dstCluster) {
    super();
    this.srcCluster = srcCluster;
    this.dstCluster = dstCluster;
  }

  @Override
  public boolean checkIsValidUnification() {
    try {
      final String srcAu = srcCluster.getAu();
      final String dstAu = dstCluster.getAu();
      final Pattern pattern = Pattern.compile(ReplacementUtils.REGEX);
      final Matcher srcMatcher = pattern.matcher(srcAu);
      final Matcher dstMatcher = pattern.matcher(dstAu);
      if (srcMatcher.find()) {
        return false;
      }
      if (dstMatcher.find()) {
        return false;
      }
      final String srcEq = TemplateUtils.removeAll(srcAu);
      final String dstEq = TemplateUtils.removeAll(dstAu);
      final AntiUnifier un = UnifierCluster.computeUnification(srcEq, dstEq);
      final List<VariableWithHedges> vs = un.getValue().getVariables();
      if (vs.size() != 1) {
        return false;
      }
      return true;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
