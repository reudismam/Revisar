package br.ufcg.spg.compile;

import br.ufcg.spg.bean.CommitFile;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.cache.CunitCallable;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.edit.EditStorage;
import br.ufcg.spg.git.GitUtils;
import br.ufcg.spg.main.MainArguments;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.project.Version;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

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

  public static Tuple<CompilationUnit, CompilationUnit> getCompilationUnits(
          Edit srcEdit, Edit dstEdit) throws IOException {
    Tuple<String, String> tu = getBeforeAfterFile(srcEdit, dstEdit);
    CompilationUnit srcUnit = JParser.parse("temp1.java", tu.getItem1());
    CompilationUnit dstUnit = JParser.parse("temp2.java", tu.getItem2());
    return new Tuple<>(srcUnit, dstUnit);
  }

  public static Tuple<String, String> getBeforeAfterFile(
          Edit srcEdit, Edit dstEdit) throws IOException {
    MainArguments main = MainArguments.getInstance();
    Repository repository = GitUtils.startRepo(main.getProjectFolder() + "/"
            + dstEdit.getProject());
    RevCommit revCommit = GitUtils.extractCommit(repository, dstEdit.getCommit());
    RevCommit befCommit = new GitUtils().getPrevHash(repository, revCommit);
    String before = GitUtils.getEditedFile(repository, befCommit.getTree(), srcEdit.getPath());
    String after = GitUtils.getEditedFile(repository, revCommit.getTree(), dstEdit.getPath());
    FileUtils.writeStringToFile(new File("temp1.java"), before);
    FileUtils.writeStringToFile(new File("temp2.java"), after);
    return new Tuple<>(before, after);
  }
}
