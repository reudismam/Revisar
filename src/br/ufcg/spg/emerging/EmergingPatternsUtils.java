package br.ufcg.spg.emerging;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.transformation.TransformationUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
    StringBuilder builder = new StringBuilder();
    for (int year = 2018; year >= 2000; year--) {
      Tuple<List<Cluster>, List<Cluster>> tu = emergingPatterns(year, srcClusters);
      List<Cluster> emerging = tu.getItem1();
      List<Cluster> old = tu.getItem2();
      builder.append(emerging.size() + " : " + old.size() + "\n");
      String emergingPath = "../Projects/" + year + "/Emerging/";
      String oldPath = "../Projects/" + year + "/Old/";
      TransformationUtils.transformations(emergingPath, emerging);
      TransformationUtils.transformations(oldPath, old);
    }
    try {
      FileUtils.writeStringToFile(new File("../Projects/emerging_by_year.txt"), builder.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Computes the matches for all clusters.
   */
  public static Tuple<List<Cluster>, List<Cluster>> emergingPatterns(
      int year, List<Cluster> srcClusters) {
    logger.trace("COMPUTING EMERGING CLUSTERS");
    List<Cluster> emerging = new ArrayList<>();
    List<Cluster> old = new ArrayList<>();
    for (Cluster srcCluster : srcClusters) {
      List<Edit> edits = srcCluster.getNodes();
      edits = new ArrayList<>(edits);
      // Sorting
      Collections.sort(edits, Collections.reverseOrder(new SortbyDate()));
      Edit last = edits.get(edits.size() - 1);
      System.out.println(last.getDate());
      Date threshould = new GregorianCalendar(year, Calendar.JANUARY, 01).getTime();
      if (last.getDate().after(threshould)) {
        emerging.add(srcCluster);
      } else {
        old.add(srcCluster);
      }
    }
    return new Tuple<>(emerging, old);
  }
}
