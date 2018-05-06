package br.ufcg.spg.analyzer.test;

import at.jku.risc.stout.urauc.algo.JustificationException;
import at.jku.risc.stout.urauc.util.ControlledException;
import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.cluster.ClusterUnifier;
import br.ufcg.spg.database.ClusterDao;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.main.MainArguments;
import br.ufcg.spg.transformation.TransformationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FunctionalTesting {
  
  @Test
  public void testRefasterRuleCreation() 
      throws IOException, JustificationException, ControlledException, CoreException {
    configMainArguments();
    String clusterId = "1494576";
    ClusterDao dao = ClusterDao.getInstance();
    List<Cluster> clusters = dao.getClusters(clusterId);
    List<Edit> srcEdits = clusters.get(0).getNodes();
    final ClusterUnifier unifierCluster = ClusterUnifier.getInstance();
    try {
      List<Cluster> srcClusters = unifierCluster.clusterEdits(srcEdits);
      for (Cluster cluster : srcClusters) {
        List<String> refasterRules = new ArrayList<>();
        for (Edit edit : cluster.getNodes()) {
          String refaster = TransformationUtils.tranformation(cluster, edit);
          refasterRules.add(refaster);
        }
        boolean equals = refasterRules.stream().allMatch(o -> o.equals(refasterRules.get(0)));
        assertTrue(equals);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void configMainArguments() {
    final MainArguments arguments = MainArguments.getInstance();
    arguments.setProjects("projects.txt");
    arguments.setProjectFolder("../Projects");
  }
}