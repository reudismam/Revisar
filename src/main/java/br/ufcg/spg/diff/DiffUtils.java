package br.ufcg.spg.diff;

import br.ufcg.spg.bean.CommitFile;
import br.ufcg.spg.cache.DiffCallable;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.edit.EditStorage;
import br.ufcg.spg.project.ProjectAnalyzer;
import br.ufcg.spg.project.ProjectInfo;

import java.util.concurrent.ExecutionException;

/**
 * Utility class to perform diff.
 * @author Usuário
 *
 */
public class DiffUtils {
  
  private DiffUtils() {
  }
  
  /**
   * Calculates the diff.
   * @param srcEdit source edit
   * @param dstEdit destination edit
   */
  public static DiffCalculator diff(final Edit srcEdit, final Edit dstEdit) 
      throws ExecutionException {  
    final ProjectInfo pinfo = ProjectAnalyzer.project(srcEdit);
    final DiffCallable callable = new DiffCallable(pinfo, srcEdit, dstEdit);
    final CommitFile key = new CommitFile(dstEdit.getCommit(), dstEdit.getPath());
    final EditStorage storage = EditStorage.getInstance();
    return storage.getDiff(key, callable);
  }
}
