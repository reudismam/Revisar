package br.ufcg.spg.compile;

import br.ufcg.spg.bean.CommitFile;
import br.ufcg.spg.cache.CunitCallable;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.edit.EditStorage;
import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.project.Version;

import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class CompilerUtils {
  
  private CompilerUtils() {
  }
  
  /**
   * Gets compilation unit.
   * @param edit edit
   * @param commit commit
   * @param version version
   * @param pinfo project information
   * @return Compilation unit.
   */
  public static CompilationUnit getCunit(final Edit edit, final String commit, 
      final Version version, final ProjectInfo pinfo) 
      throws ExecutionException {
    final CunitCallable loader = new CunitCallable(commit, edit, version, pinfo);
    final EditStorage storage = EditStorage.getInstance();
    final CommitFile key = new CommitFile(commit, edit.getPath());
    return storage.getCunit(key, loader);
  }
}
