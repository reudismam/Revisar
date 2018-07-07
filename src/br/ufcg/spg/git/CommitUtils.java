package br.ufcg.spg.git;

import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.project.Version;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Commit utility class.
 */
public class CommitUtils {
  
  private CommitUtils() {
  }
  
  static final Logger logger = LogManager.getLogger(CommitUtils.class.getName());
  
  /**
   * Checkout source code if current commit differ from provided commit.
   * @param commit commit
   * @param pi project information
   */
  public static void checkoutIfDiffer(final String commit, final ProjectInfo pi) 
      throws IOException, GitAPIException {
    final GitUtils analyzer = new GitUtils();
    final Version srcVersion = pi.getSrcVersion();
    final Version dstVersion = pi.getDstVersion();
    final String currentCommit = analyzer.getCommit(dstVersion.getProject());
    final String acommit = commit.substring(0, 7);
    final String acurrent = currentCommit.substring(0, 7);
    if (!acommit.equals(acurrent)) {
      logger.trace("CHECKOUTING CURRENT VERSION...");
      GitUtils.checkout(dstVersion.getProject(), commit);
      final String previous = analyzer.getPrevHash(dstVersion.getProject(), commit);
      logger.trace("CHECKOUTING CURRENT VERSION...");
      GitUtils.checkout(srcVersion.getProject(), previous);  
    }
  }
}
