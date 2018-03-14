package br.ufcg.spg.evaluator;

import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.evaluator.template.ITransformationValidatorStrategy;
import br.ufcg.spg.evaluator.template.TemplateValidatorStrategy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;

public class ClusterValidator {
  
  /**
   * Verifies the validity of the transformation.
   * @param srcCluster source code cluster
   * @return true if transformation is valid.
   */
  public static boolean isValidTrans(final Cluster srcCluster) 
      throws IOException, NoFilepatternException, GitAPIException, ExecutionException {
    final Cluster dstCluster = srcCluster.getDst();
    final List<Edit> srcEdits = srcCluster.getNodes();
    final String srcAu = srcCluster.getAu();
    final String dstAu = dstCluster.getAu();
    final ITransformationValidatorStrategy strategy = new TemplateValidatorStrategy();
    final boolean valid = strategy.isValidTrans(srcEdits, srcAu, dstAu);
    return valid;
  }
  
  /**
   * Verifies the validity of the transformation
   * @param csrcEdits source code cluster
   * @param srcAu source unification for all edits
   * @param dstAu destination unification for all edits
   * @return true if transformation is valid.
   */
  public static boolean isValidTrans(final List<Edit> csrcEdits, 
      final String srcAu, final String dstAu) 
      throws IOException, NoFilepatternException, GitAPIException, ExecutionException {
    final ITransformationValidatorStrategy strategy = new TemplateValidatorStrategy();
    // Since edits are consistent in cluster, we only need two edits
    final Edit firstEdit = csrcEdits.get(0);
    final Edit lastEdit = csrcEdits.get(csrcEdits.size() - 1);
    final Edit[] srcAnalyzed = { firstEdit, lastEdit };
    final List<Edit> srcEdits = Arrays.asList(srcAnalyzed);
    return strategy.isValidTrans(srcEdits, srcAu, dstAu);
  }
}
