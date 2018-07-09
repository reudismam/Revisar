package br.ufcg.spg.ml.traversal;

import br.ufcg.spg.ml.editoperation.IEditNode;
import br.ufcg.spg.ml.editoperation.Script;

import java.util.HashSet;
import java.util.Set;

import org.christopherfrantz.dbscan.DBSCANClusteringException;
import org.christopherfrantz.dbscan.DistanceMetric;

public class ScriptDistanceMetric implements DistanceMetric<Script> {

  @Override
  public double calculateDistance(Script arg0, Script arg1) throws DBSCANClusteringException {
    Set<IEditNode> set = new HashSet<>();
    for (IEditNode node : arg0.getList()) {
      set.add(node);
    }
    double common = 0.0;
    for (IEditNode node : arg1.getList()) {
      if (set.contains(node)) {
        common++;
      }
    }
    return 1.0 -  ((2.0 * common) / (arg0.getList().size() + arg1.getList().size()));
  }
}
