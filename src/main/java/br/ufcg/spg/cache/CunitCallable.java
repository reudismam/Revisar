package br.ufcg.spg.cache;

import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.git.CommitUtils;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.project.Version;

import java.util.concurrent.Callable;

import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Creates a compilation unit.
 *
 */
public class CunitCallable implements Callable<CompilationUnit> {
  
  /**
   * Commit.
   */
  private final transient String commit;
  
  /**
   * Edit to be analyzed.
   */
  private final transient Edit edit;
  
  /**
   * Project version.
   */
  private final transient Version version;
  
  /**
   * Project information.
   */
  private final transient ProjectInfo pinfo;
  
  /**
   * Constructor.
   * @param edit edit
   * @param version version
   * @param pinfo project info
   */
  public CunitCallable(final String commit, final Edit edit, final Version version,
      final ProjectInfo pinfo) {
    super();
    this.commit = commit;
    this.edit = edit;
    this.version = version;
    this.pinfo = pinfo;
  }

  /**
   * Gets compilation unit.
   * @return Compilation unit.
   */
  @Override
  public CompilationUnit call() throws Exception {
    final String path = edit.getPath();
    CommitUtils.checkoutIfDiffer(commit, pinfo);
    final CompilationUnit cunit = JParser.parse(path, version);
    return cunit;
  }
}
