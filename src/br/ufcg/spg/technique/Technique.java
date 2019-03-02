package br.ufcg.spg.technique;

import br.ufcg.spg.bean.EditFile;
import br.ufcg.spg.cluster.ClusterUtils;
import br.ufcg.spg.dependence.DependenceUtils;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.edit.EditStorage;
import br.ufcg.spg.editpair.EditExtractorStrategy;
import br.ufcg.spg.log.TimeLogger;
import br.ufcg.spg.transformation.TransformationUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * Main class for the technique.
 */
public final class Technique {
  
  private Technique(){
  }
  
  /**
   * Appends edits to be analyzed.
   * @param project
   *          project folder that contains .git folder
   * @param files
   *          files to be analyzed. Pass null if you want all commit modified
   *          files
   * @param commit
   *          commit-id
   */
  public static void addEdits(final String project, final List<EditFile> files, 
      final RevCommit commit) {
    try {
      final List<Edit> srcEdits = EditExtractorStrategy.computeEditPairs(project, files, commit);
      final EditStorage storage = EditStorage.getInstance();
      storage.addAllSrcEdit(srcEdits);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Appends edits to be analyzed.
   * @param project
   *          project folder that contains .git folder
   * @param commit
   *          commit-id
   */
  public static void addEdits(final String project, final RevCommit commit) {
    addEdits(project, null, commit);
  }
  
  /**
   * Cluster edits.
   */
  public static void clusterEdits() {
    // Cluster edits
    final long startTime = System.nanoTime();  
    try {
      ClusterUtils.buildClusters();
    } catch (final OutOfMemoryError exception) {
      final EditStorage storage = EditStorage.getInstance();
      storage.getCunits().cleanUp();
      storage.getDiffs().cleanUp();
      clusterEdits();
    }
    final long estimatedTime = System.nanoTime() - startTime;
    TimeLogger.getInstance().setTimeCluster(estimatedTime);
  }
  
  public static void computeDepedence() 
      throws GitAPIException, IOException, ExecutionException {
    DependenceUtils.dependences();
  }
  
  /**
   * Analyze edits.
   */
  public static void translateEdits() {
    final long startTime = System.nanoTime(); 
    try {
      TransformationUtils.transformations();
    } catch (final OutOfMemoryError e) {
      EditStorage.getInstance().getCunits().cleanUp();
      EditStorage.getInstance().getDiffs().cleanUp();
      translateEdits();
    }
    final long estimatedTime = System.nanoTime() - startTime;
    TimeLogger.getInstance().setTimeTransform(estimatedTime);
  }
  
  /**
   * Translate edits for a specific cluster.
   * @param clusterId cluster id
   */
  public static void translateEdits(final String clusterId) {
    TransformationUtils.transformations(clusterId);
  }
}
