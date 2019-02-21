package br.ufcg.spg.edit;

import br.ufcg.spg.bean.CommitFile;
import br.ufcg.spg.database.EditDao;
import br.ufcg.spg.diff.DiffCalculator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jgit.revwalk.RevCommit;

public class EditStorage {
  private Map<String, List<RevCommit>> commitProjects;
  private Cache<CommitFile, CompilationUnit> cunits;
  private Cache<CommitFile, DiffCalculator> diffs;

  public static final int SRC = 1;
  public static final int DST = 2;
  public static final int FIXED_SRC = 3;
  public static final int FIXED_DST = 4;

  private int maxNumberEdits = 100;
  private int numberEdits = 0;
  private RevCommit currentCommit;

  private static EditStorage instance;

  private EditStorage() {
    init();
  }
  
  /**
   * Gets singleton instance.
   * 
   * @return singleton instance
   */
  public static synchronized EditStorage getInstance() {
    if (instance == null) {
      instance = new EditStorage();
    }
    return instance;
  } 

  public Cache<CommitFile, CompilationUnit> getCunits() {
    return cunits;
  }

  public void setCunits(final Cache<CommitFile, CompilationUnit> cunits) {
    this.cunits = cunits;
  }

  public Cache<CommitFile, DiffCalculator> getDiffs() {
    return diffs;
  }

  public void setDiffs(final Cache<CommitFile, DiffCalculator> diffs) {
    this.diffs = diffs;
  }

  /**
   * Adds compilation unit for provided key.
   * @param key key
   * @param cunit compilation unit
   */
  public void addCunit(final CommitFile key, final CompilationUnit cunit) {
    if (cunits != null) {
      cunits.put(key, cunit);
    }
  }
  
  /**
   * Gets compilation unit.
   * @param key key
   * @return compilation unit
   */
  public CompilationUnit getCunit(final CommitFile key, final Callable<CompilationUnit> callale) 
      throws ExecutionException {
    return cunits.get(key, callale);
  }
  
  /**
   * Gets diff.
   * @param key key
   * @return diff file for the provided key
   */
  public DiffCalculator getDiff(final CommitFile key, final Callable<DiffCalculator> callable) 
      throws ExecutionException {
    return diffs.get(key, callable);
  }

  /**
   * Initiates list.
   */
  public void init() {
    this.commitProjects = new TreeMap<>();
    cunits = CacheBuilder.newBuilder().maximumSize(100).build();
    diffs = CacheBuilder.newBuilder().maximumSize(100).build();

  }

  public int getMaxNumberEdits() {
    return maxNumberEdits;
  }

  public void setMaxNumberEdits(final int maxNumberEdits) {
    this.maxNumberEdits = maxNumberEdits;
  }
  
  public void incrementNumberEdits(final int i) {
    numberEdits += i;
  }
  
  public void incrementNumberEdits() {
    numberEdits++;
  }

  public int getNumberEdits() {
    return numberEdits;
  }

  public void setNumberEdits(final int numberEdits) {
    this.numberEdits = numberEdits;
  }

  public RevCommit getCurrentCommit() {
    return currentCommit;
  }

  public Map<String, List<RevCommit>> getCommitProjects() {
    return commitProjects;
  }

  public void setCommitProjects(final Map<String, List<RevCommit>> commitsProjects) {
    this.commitProjects = commitsProjects;
  }
  
  /**
   * Adds a commit to a project.
   * @param project project
   * @param commit commit to be added.
   */
  public void addCommitProject(final String project, final RevCommit commit) {
    if (!this.commitProjects.containsKey(project)) {
      this.commitProjects.put(project, new ArrayList<>());
    }
    this.commitProjects.get(project).add(commit);
  }

  public void setCurrentCommit(final RevCommit currentCommit) {
    this.currentCommit = currentCommit;
  }

  /**
   * Adds source edit.
   * @param edit edit
   */
  public void addSrcEdit(final Edit edit) {
    incrementNumberEdits();
    final EditDao dao = EditDao.getInstance();
    dao.save(edit);
  }
  
  /**
   * Adds a list of edits.
   * @param edits list of edits to be added.
   */
  public void addAllSrcEdit(final List<Edit> edits) {
    incrementNumberEdits(edits.size());
    final EditDao dao = EditDao.getInstance();
    dao.saveAll(edits);
  }

  /**
   * Gets all source nodes list.
   * 
   * @return all source node list
   */
  public List<Edit> getSrcList() {
    final EditDao dao = EditDao.getInstance();
    return dao.getAll();
  }
  
  /**
   * Gets all source nodes list.
   * 
   * @return all source node list
   */
  public List<Edit> getSrcList(final String commit) {
    final EditDao dao = EditDao.getInstance();
    return dao.getSrcEdits(commit);
  }
  
  /**
   * Gets all source nodes list.
   * 
   * @return all source node list
   */
  public List<Edit> getSrcListByDCap(final String dcap, final int d) {
    final EditDao dao = EditDao.getInstance();
    return dao.getSrcEditsByDcap(dcap, d);
  }

  /**
   * Gets all caps.
   * @return all caps.
   */
  public List<String> getAllDCaps(final int d) {
    final EditDao editDao = EditDao.getInstance();
    return editDao.getAllDcaps(d);
  }
}