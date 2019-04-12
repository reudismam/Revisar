package br.ufcg.spg.git;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class RepositoryStore {
  private static RepositoryStore instance;
  private Map<String, Repository> repositories;
  
  private RepositoryStore() {
	  repositories = new HashMap<>();
  }
  
  public static RepositoryStore getInstance() {
	  if (instance == null) {
		  instance = new RepositoryStore();
	  }
	  return instance;
  }
  
  public Repository getRepository(String repository) {
	  if (!repositories.containsKey(repository)) {
		try {
			 Repository repo = startRepo(repository);
			repositories.put(repository, repo);
		} catch (IOException e) {
			e.printStackTrace();
		}		  
	  }
	  return repositories.get(repository);
  }
  
  /**
   * Starts the repository.
   * 
   * @param repoPath
   *          repository path
   * @return repository
   */
  private static Repository startRepo(final String repoPath) throws IOException {
    final FileRepositoryBuilder builder = new FileRepositoryBuilder();
    return builder.setGitDir(new File(repoPath + "/.git")).setMustExist(true).build();
  }
}
