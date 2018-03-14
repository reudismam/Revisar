package br.ufcg.spg.diff;

import br.ufcg.spg.bean.CommitFile;
import br.ufcg.spg.cache.DiffCallable;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.edit.EditStorage;
import br.ufcg.spg.project.ProjectAnalyzer;
import br.ufcg.spg.project.ProjectInfo;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;

public class DiffUtils {
  
  /**
   * Calculates the diff.
   * @param srcEdit source edit
   * @param dstEdit destination edit
   */
  public static DiffCalculator diff(final Edit srcEdit, final Edit dstEdit) 
      throws MissingObjectException, IncorrectObjectTypeException, AmbiguousObjectException, NoFilepatternException, IOException, GitAPIException, ExecutionException {  
    final ProjectInfo pi = ProjectAnalyzer.project(srcEdit);
    final DiffCallable callable = new DiffCallable(pi, srcEdit, dstEdit);
    final CommitFile key = new CommitFile(dstEdit.getCommit(), dstEdit.getPath());
    final EditStorage storage = EditStorage.getInstance();
    return storage.getDiff(key, callable);
  }
}
