package br.ufcg.spg.analyzer.test;

import at.jku.risc.stout.urauc.algo.JustificationException;
import at.jku.risc.stout.urauc.util.ControlledException;
import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.cluster.ClusterUtils;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.main.MainArguments;
import br.ufcg.spg.transformation.Transformation;
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
    final String clusterId = "1488922";
    try {
      final List<Cluster> srcClusters = ClusterUtils.buildClustersSegmentByType(clusterId);
      for (final Cluster cluster : srcClusters) {
        final List<String> refasterRules = new ArrayList<>();
        for (final Edit edit : cluster.getNodes()) {
          final Transformation transformation = TransformationUtils.tranformation(cluster);
          final String refaster = TransformationUtils.createRefasterRule(cluster, edit);
          refasterRules.add(refaster);
          TransformationUtils.saveTransformation(transformation, edit);
        }
        final boolean equals = refasterRules.stream().allMatch(o -> o.equals(refasterRules.get(0)));
        assertTrue(equals);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void configMainArguments() {
    final MainArguments arguments = MainArguments.getInstance();
    arguments.setProjects("projects.txt");
    arguments.setProjectFolder("../Projects");
  }
}