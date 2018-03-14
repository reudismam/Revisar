package br.ufcg.spg.git;

import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.project.Version;

import java.io.IOException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;

public class CommitUtils {
  
  /**
   * Checkout source code if current commit differ from provided commit.
   * @param commit commit
   * @param pi project information
   */
  public static void checkoutIfDiffer(final String commit, final ProjectInfo pi) 
      throws MissingObjectException, IncorrectObjectTypeException, 
      AmbiguousObjectException, IOException, NoFilepatternException, GitAPIException {
    final GitUtils analyzer = new GitUtils();
    final Version srcVersion = pi.getSrcVersion();
    final Version dstVersion = pi.getDstVersion();
    final String currentCommit = analyzer.getCommit(dstVersion.getProject());
    final String acommit = commit.substring(0, 7);
    final String acurrent = currentCommit.substring(0, 7);
    if (!acommit.equals(acurrent)) {
      System.out.println("CHECKOUTING CURRENT VERSION...");
      GitUtils.checkout(dstVersion.getProject(), commit);
      final String previous = analyzer.getPrevHash(dstVersion.getProject(), commit);
      System.out.println("CHECKOUTING CURRENT VERSION...");
      GitUtils.checkout(srcVersion.getProject(), previous);  
    }
  }
}
