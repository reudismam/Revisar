package br.ufcg.spg.ml.metric;

import br.ufcg.spg.ml.editoperation.EditNode;
import br.ufcg.spg.ml.editoperation.Script;
import de.jail.geometry.distancefunctions.PointBasedDistanceFunction;
import de.jail.geometry.schemas.Point;

public class ScriptDistanceMetric<T> implements PointBasedDistanceFunction {

  @Override
  public double calculate(Point point0, Point point1) {
    @SuppressWarnings("unchecked")
    Script<T> arg0 = (Script<T>) point0;
    @SuppressWarnings("unchecked")
    Script<T> arg1 = (Script<T>) point1;
    double common = 0.0;
    boolean[] duplicates = new boolean[arg0.getList().size()];
    for (int i = 0; i < arg1.getList().size(); i++) {
      EditNode<T> node1 = arg1.getList().get(i);
      for (int j = 0; j < arg0.getList().size(); j++) {
        EditNode<T> node0 = arg0.getList().get(j);
        if (node1.equals(node0) && !duplicates[j]) {
          common++;
          duplicates[j] = true;
          break;
        }
      }
    }
    return 1.0 - ((2.0 * common) / (arg0.getList().size() + arg1.getList().size()));
  }
}
