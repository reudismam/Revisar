package br.ufcg.spg.bean;

public class CommitFile implements Comparable<CommitFile> {

  private final String commit;
  private final String filePath;

  /**
   * Constructs a new commit file.
   * 
   * @param commit
   *          commit
   * @param filePath
   *          file path
   */
  public CommitFile(final String commit, final String filePath) {
    this.commit = commit;
    this.filePath = filePath;
  }

  public String getCommit() {
    return commit;
  }

  public String getFilePath() {
    return filePath;
  }

  @Override
  public String toString() {
    return commit.toString() + ", " + filePath.toString();
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * Verifies if two objects are equals.
   */
  @Override
  public boolean equals(final Object obj) {
    final CommitFile other = (CommitFile) obj;
    if (!this.getCommit().equals(other.getCommit())) {
      return false;
    }
    if (!this.getFilePath().equals(other.getFilePath())) {
      return false;
    }
    return true;
  }

  /**
   * Compares this commit file other commit file.
   */
  @Override
  public int compareTo(final CommitFile o) {
    return toString().compareTo(o.toString());
  }
}
