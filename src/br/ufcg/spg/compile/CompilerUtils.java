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
  /**
   * Gets compilation unit
   * @param edit edit
   * @param commit commit
   * @param version version
   * @param pi project information
   * @return Compilation unit.
   */
  public static CompilationUnit getCunit(final Edit edit, final String commit, 
      final Version version, final ProjectInfo pi) 
      throws ExecutionException {
    final CunitCallable loader = new CunitCallable(commit, edit, version, pi);
    final EditStorage storage = EditStorage.getInstance();
    final CommitFile key = new CommitFile(commit, edit.getPath());
    final CompilationUnit cunit = storage.getCunit(key, loader);
    return cunit;
  }
}
