package br.ufcg.spg.validator;

import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.validator.template.ITransformationValidatorStrategy;
import br.ufcg.spg.validator.template.TemplateValidatorStrategy;

import java.util.Arrays;
import java.util.List;

/**
 * Cluster evaluator.
 *
 */
public class ClusterValidator {
  
  private ClusterValidator() {
  }
  
  /**
   * Verifies the validity of the transformation.
   * @param srcCluster source code cluster
   * @return true if transformation is valid.
   */
  public static boolean isValidTrans(final Cluster srcCluster) {
    final Cluster dstCluster = srcCluster.getDst();
    final List<Edit> srcEdits = srcCluster.getNodes();
    final String srcAu = srcCluster.getAu();
    final String dstAu = dstCluster.getAu();
    final ITransformationValidatorStrategy strategy = new TemplateValidatorStrategy();
    return strategy.isValidTrans(srcEdits, srcAu, dstAu);
  }
  
  /**
   * Verifies the validity of the transformation
   * @param csrcEdits source code cluster
   * @param srcAu source unification for all edits
   * @param dstAu destination unification for all edits
   * @return true if transformation is valid.
   */
  public static boolean isValidTrans(final List<Edit> csrcEdits, 
      final String srcAu, final String dstAu) {
    final ITransformationValidatorStrategy strategy = new TemplateValidatorStrategy();
    // Since edits are consistent in cluster, we only need two edits
    final Edit firstEdit = csrcEdits.get(0);
    final Edit lastEdit = csrcEdits.get(csrcEdits.size() - 1);
    final Edit[] srcAnalyzed = { firstEdit, lastEdit };
    final List<Edit> srcEdits = Arrays.asList(srcAnalyzed);
    return strategy.isValidTrans(srcEdits, srcAu, dstAu);
  }
}
