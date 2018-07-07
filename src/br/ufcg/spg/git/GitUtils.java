package br.ufcg.spg.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

/**
 * Git utility class.
 */
public class GitUtils {
  
  static final Logger logger = LogManager.getLogger(GitUtils.class.getName());
  
  /**
   * Checkouts commit from the specified repository given a hash value.
   * 
   * @param repositoryPath
   *          repository
   * @param sha
   *          commit id
   */
  public static void checkout(final String repositoryPath, final String sha) 
      throws IOException, GitAPIException {
    try (final Git git = Git.open(new File(repositoryPath))) { // checkout the folder with .git
      try {
        final CheckoutCommand checkout = git.checkout();
        checkout.setName(sha).call();
        logger.trace("Branch");
        logger.trace(git.getRepository().getFullBranch());
      } catch (final Exception e) {
        final String message = e.getMessage();
        final String meStart = "Cannot lock ";
        final String meEnd = ".git\\index";
        final int pathStart = message.indexOf(meStart);
        final int pathEnd = message.indexOf(meEnd);
        if (pathStart != -1 && pathEnd != -1) {
          String path = message.substring(pathStart + meStart.length(), pathEnd + meEnd.length());
          path = path + ".lock";
          try {
            final File file = new File(path);
            file.setWritable(true);
            Files.delete(file.toPath());
          } catch (final IOException e1) {
            e1.printStackTrace();
          }
        }
        try {
          final GitUtils gitAnalyzer = new GitUtils();
          gitAnalyzer.clearLocalEdits(repositoryPath);
          final CheckoutCommand checkout = git.checkout();
          checkout.setName(sha).call();
          logger.trace("Branch");
          logger.trace(git.getRepository().getFullBranch());
        } catch (final Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  /**
   * Computes the diff for the commit.
   * 
   * @param repoPath
   *          path to the folder that contains the .git folder
   * @param hashId
   *          id of the commit
   * @return diff for the commit
   */
  public String diffCommit(final String repoPath, final String hashId) throws IOException {
    // Initialize repositories.
    final Repository repository = startRepo(repoPath);
    // Get the commit you are looking for.
    final RevCommit newCommit = extractCommit(repository, hashId);
    logger.trace("LogCommit: " + newCommit);
    final String logMessage = newCommit.getFullMessage();
    logger.trace("LogMessage: " + logMessage);
    // Diff of the commit with the previous one.
    return getDiffOfCommit(repository, newCommit);
  }

  /**
   * Extracts commit.
   * 
   * @param repoPath
   *          repository
   * @param hashId
   *          hash
   * @return commit
   */
  public RevCommit extractCommit(final String repoPath, final String hashId)
      throws IOException {
    final Repository repository = startRepo(repoPath);
    return extractCommit(repository, hashId);
  }

  /**
   * Extracts the commit given the commit id.
   * 
   * @param repository
   *          folder that contains the .git folder
   * @param hashId
   *          commit id
   * @return commit for the commit id
   */
  public RevCommit extractCommit(final Repository repository, final String hashId)
      throws IOException {
    RevCommit newCommit;
    try (RevWalk walk = new RevWalk(repository)) {
      newCommit = walk.parseCommit(repository.resolve(hashId));
      walk.dispose();
    }
    return newCommit;
  }

  /**
   * Gets file from a commit.
   * 
   * @param repoPath
   *          repository
   * @param file
   *          file path
   * @param hashId
   *          id of the commit
   * @return file from commit
   */
  public String fileCommit2(final String repoPath, final String file, final String hashId)
      throws IOException {
    final Repository repository = startRepo(repoPath);
    final RevCommit commit = extractCommit(repoPath, hashId);
    // and using commit's tree find the path
    final RevTree tree = commit.getTree();
    // now try to find a specific file
    try (TreeWalk treeWalk = new TreeWalk(repository)) {
      treeWalk.addTree(tree);
      treeWalk.setRecursive(true);
      treeWalk.setFilter(PathFilter.create(file));
      if (!treeWalk.next()) {
        throw new IllegalStateException("Did not find expected file " + "'" + file + "");
      }
      final ObjectId objectId = treeWalk.getObjectId(0);
      final ObjectLoader loader = repository.open(objectId);
      return  new String(loader.getBytes(), "UTF-8");
    }
  }

  /**
   * Gets the commit given a repository.
   * 
   * @param repo
   *          path to the folder that contains the .git folder
   * @return current commit
   */
  public String getCommit(final String repo)
      throws IOException {
    final Repository repository = startRepo(repo);
    try (RevWalk walk = new RevWalk(repository)) {
      final ObjectId current = repository.resolve(Constants.HEAD);
      return current.name();
    }
  }

  /**
   * Starts the repository.
   * 
   * @param repoPath
   *          repository path
   * @return repository
   */
  private Repository startRepo(final String repoPath) throws IOException {
    final FileRepositoryBuilder builder = new FileRepositoryBuilder();
    return builder.setGitDir(new File(repoPath + "/.git")).setMustExist(true).build();
  }

  /**
   * Gets the diff as a string.
   * 
   * @param commit
   *          commit id
   */
  private String getDiffOfCommit(final Repository repository, 
      final RevCommit commit) throws IOException {
    // Get commit that is previous to the current one.
    final RevCommit oldCommit = getPrevHash(repository, commit);
    if (oldCommit == null) {
      return "Start of repo";
    }
    // Use treeIterator to diff.
    final AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(repository, oldCommit);
    final AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(repository, commit);
    final OutputStream outputStream = new ByteArrayOutputStream();
    try (DiffFormatter formatter = new DiffFormatter(outputStream)) {
      formatter.setRepository(repository);
      formatter.format(oldTreeIterator, newTreeIterator);
    }
    return outputStream.toString();
  }

  /**
   * Gets the previous commit.
   * 
   * @param hashId
   *          commit id
   * @return previous commit
   */
  public String getPrevHash(final String repoPath, final String hashId) throws IOException {
    final Repository repository = startRepo(repoPath);
    final RevCommit commit = extractCommit(repository, hashId);
    final RevCommit previous = getPrevHash(repository, commit);
    return previous.getId().name();
  }

  /**
   * Gets the previous commit.
   * 
   * @param commit
   *          commit id
   * @return previous commit
   */
  public RevCommit getPrevHash(final Repository repository, 
      final RevCommit commit) throws IOException {
    try (RevWalk walk = new RevWalk(repository)) {
      // Starting point
      walk.markStart(commit);
      int count = 0;
      for (final RevCommit rev : walk) {
        // got the previous commit.
        if (count == 1) {
          return rev;
        }
        count++;
      }
      walk.dispose();
    }
    // Reached end and no previous commits.
    return null;
  }

  /**
   * Extracts log identifications.
   * 
   * @param repoPath
   *          repository
   * @return log identification
   */
  public List<String> gitLog(final String repoPath) throws IOException, GitAPIException {
    final Repository repository = startRepo(repoPath);
    try (Git git = new Git(repository)) {
      final Iterable<RevCommit> logs = git.log().call();
      final List<String> log = new ArrayList<>();
      for (final RevCommit rev : logs) {
        logger.trace("Commit: " + rev.getId().getName());
        log.add(rev.getId().getName());
      }
      return log;
    }
  }

  /**
   * Extracts log identifications.
   * 
   * @param repoPath
   *          repository
   * @return log identification
   */
  public List<String> getEmail(final String repoPath) throws IOException, GitAPIException {
    final Repository repository = startRepo(repoPath);
    try (Git git = new Git(repository)) {
      final Iterable<RevCommit> logs = git.log().call();
      final Set<String> log = new HashSet<>();
      for (final RevCommit rev : logs) {
        logger.trace("Commit: " + rev.getId().getName());
        final String email = rev.getAuthorIdent().getEmailAddress().trim();
        if (EmailValidator.getInstance().isValid(email)) {
          log.add(email);
        }
      }
      return new ArrayList<>(log);
    }
  }

  /**
   * Helper function to get the tree of the changes in a commit.
   * 
   * @param repository
   *          folder that contains the .git folder
   * @param commitId
   *          id of the commit
   * @return tree of the changes in a commit
   */
  private AbstractTreeIterator getCanonicalTreeParser(final Repository repository, 
      final ObjectId commitId)
      throws IOException {
    try (RevWalk walk = new RevWalk(repository)) {
      final RevCommit commit = walk.parseCommit(commitId);
      final ObjectId treeId = commit.getTree().getId();
      try (ObjectReader reader = repository.newObjectReader()) {
        return new CanonicalTreeParser(null, reader, treeId);
      }
    }
  }

  /**
   * Gets the modified files.
   * 
   * @param repoPath
   *          path to the folder that contains the .git folder
   * @param hashId
   *          id of the commit
   * @return modified files
   */
  public List<String> modifiedFiles(final String repoPath, final String hashId) {
    try {
      // Initialize repositories.
      final Repository repo = startRepo(repoPath);
      // Get the commit you are looking for.
      final RevCommit commit = extractCommit(repo, hashId);
      final RevCommit previous = getPrevHash(repo, commit);
      if (previous == null) {
        return null;
      }
      final RevCommit[] parents = commit.getParents();
      final int size = parents.length;
      // does not analyze commits that are merges
      if (size > 1) {
        return new ArrayList<>();
      }
      // does not analyze commits that are merges
      if (commit.getFullMessage().toLowerCase().contains("merge")) {
        return new ArrayList<>();
      }
      try (DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
        df.setRepository(repo);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        final List<DiffEntry> diffs = df.scan(previous.getTree(), commit.getTree());
        final List<String> modifiedFiles = new ArrayList<>();
        for (final DiffEntry diff : diffs) {
          if (diff.getChangeType() != ChangeType.MODIFY) {
            continue;
          }
          final String modifiedFile = diff.getNewPath();
          if (modifiedFile.endsWith(".java")) {
            modifiedFiles.add(modifiedFile);
          }
        }
        return modifiedFiles;
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Gets the modified files.
   * 
   * @param repoPath
   *          path to the folder that contains the .git folder
   * @param hashId
   *          id of the commit
   * @return modified files
   */
  public PersonIdent getPersonIdent(final String repoPath, final String hashId) {
    try {
      // Initialize repositories.
      final Repository repo = startRepo(repoPath);
      // Get the commit you are looking for.
      final RevCommit commit = extractCommit(repo, hashId);
      return commit.getAuthorIdent();
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Clears local edits.
   * 
   * @param repoPath
   *          repository
   */
  public void clearLocalEdits(final String repoPath) throws IOException, GitAPIException {
    final Repository repo = startRepo(repoPath);
    try (Git git = new Git(repo)) {
      logger.trace(repoPath);
      git.clean().setCleanDirectories(true).setIgnore(true).setForce(true).call();
      git.reset().setMode(ResetType.HARD).call();
      // final RevCommit stash =
      // git.stashCreate().setIncludeUntracked(true).call();
      // System.out.print("DEBUG: " + stash);
    }
  }
}
