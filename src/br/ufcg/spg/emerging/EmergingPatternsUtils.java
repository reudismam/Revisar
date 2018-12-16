package br.ufcg.spg.emerging;

import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.transformation.TransformationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class to perform transformations.
 */
public final class EmergingPatternsUtils {
  /**
   * Logger.
   */
  private static final Logger logger = LogManager.getLogger(EmergingPatternsUtils.class.getName());

  private EmergingPatternsUtils() {
  }

  /**
   * Computes the matches for all clusters.
   */
  public static void emergingPatterns(List<Cluster> srcClusters) {
    logger.trace("COMPUTING EMERGING CLUSTERS");
    List<Cluster> emerging = new ArrayList<>();
    List<Cluster> old = new ArrayList<>();
    for (Cluster srcCluster : srcClusters) {
      List<Edit> edits = srcCluster.getNodes();
      edits = new ArrayList<>(edits);
      // Sorting
      Collections.sort(edits, Collections.reverseOrder(new SortbyDate()));
      Edit last = edits.get(edits.size() - 1);
      if (last.getDate().getYear() > 2015) {
        emerging.add(srcCluster);
      } else {
        old.add(srcCluster);
      }
    }
    TransformationUtils.transformations("Projects/Emerging/", emerging);
    TransformationUtils.transformations("Projects/Old/", old);
  }
}
