package br.ufcg.spg.cache;

import br.ufcg.spg.diff.DiffCalculator;
import br.ufcg.spg.diff.DiffPath;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.git.CommitUtils;
import br.ufcg.spg.project.ProjectInfo;

import java.util.concurrent.Callable;

public class DiffCallable implements Callable<DiffCalculator> {
  private final ProjectInfo pi;
  private final Edit dstEdit;
  private final Edit srcEdit;

  /**
   * Constructor.
   * @param pi project information
   * @param srcEdit source edit
   * @param dstEdit destination edit
   */
  public DiffCallable(final ProjectInfo pi, final Edit srcEdit, final Edit dstEdit) {
    super();
    this.pi = pi;
    this.dstEdit = dstEdit;
    this.srcEdit = srcEdit;
  }



  @Override
  public DiffCalculator call() throws Exception {
    final DiffCalculator diff = new DiffPath(srcEdit.getPath(), dstEdit.getPath());
    CommitUtils.checkoutIfDiffer(dstEdit.getCommit(), pi);
    diff.diff();
    return diff;
  }
}
