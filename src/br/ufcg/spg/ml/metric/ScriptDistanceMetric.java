package br.ufcg.spg.ml.metric;

import br.ufcg.spg.component.ConnectedComponentManager;
import br.ufcg.spg.component.ConnectionStrategy;
import br.ufcg.spg.component.FullConnectedEditNode;
import br.ufcg.spg.ml.editoperation.EditNode;
import br.ufcg.spg.ml.editoperation.Script;
import br.ufcg.spg.ml.editoperation.UpdateNode;
import br.ufcg.spg.tree.RevisarTree;
import de.jail.geometry.distancefunctions.PointBasedDistanceFunction;
import de.jail.geometry.schemas.Point;

import java.util.ArrayList;
import java.util.List;

public class ScriptDistanceMetric<T> implements PointBasedDistanceFunction {

  /*@Override
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
  }*/
  
  @Override
  public double calculate(Point point0, Point point1) {
    @SuppressWarnings("unchecked")
    Script<T> a0 = (Script<T>) point0;
    @SuppressWarnings("unchecked")
    Script<T> a1 = (Script<T>) point1;
    List<EditNode<T>> arg0 = getEdits(a0);
    List<EditNode<T>> arg1 = getEdits(a1);
    double common = 0.0;
    boolean[] duplicates = new boolean[arg0.size()];
    for (int i = 0; i < arg1.size(); i++) {
      EditNode<T> node1 = arg1.get(i);
      for (int j = 0; j < arg0.size(); j++) {
        EditNode<T> node0 = arg0.get(j);
        if (node1.equals(node0) && !duplicates[j]) {
          common++;
          duplicates[j] = true;
          break;
        }
      }
    }
    return 1.0 - ((2.0 * common) / (arg0.size() + arg1.size()));
  }

  private List<EditNode<T>> getEdits(Script<T> arg0) {
    ConnectedComponentManager<EditNode<T>> con0 = new ConnectedComponentManager<>();
    ConnectionStrategy stt0 = new FullConnectedEditNode<>(arg0.getList());
    List<List<EditNode<T>>> a0 = con0.connectedComponents(arg0.getList(), stt0);
    List<EditNode<T>> l0 = getToCompareEdits(a0);
    List<EditNode<T>> mean0 = getMeaningfulEdits(l0);
    return mean0;
  }
  
  /**
   * Gets meaningful edits.
   */
  private List<EditNode<T>> getMeaningfulEdits(List<EditNode<T>> list) {
    List<EditNode<T>> edits = new ArrayList<>();
    for (EditNode<T> edit : list) {
      if (edit instanceof UpdateNode<?>) {
        UpdateNode<T> update = (UpdateNode<T>) edit;
        if (!(containsHash(update.getT1Node()) && containsHash(update.getTo()))) {
          edits.add(edit);
        }
      } else {
        if (!containsHash(edit.getT1Node())) {
          edits.add(edit);
        }
      }
    }
    return edits;
  }
  
  /**
   * Gets to compare edits.
   */
  private List<EditNode<T>> getToCompareEdits(List<List<EditNode<T>>> list) {
    List<EditNode<T>> edits = new ArrayList<>();
    for (List<EditNode<T>> editl : list) {
      EditNode<T> edit = editl.get(editl.size() - 1);
      edits.add(edit);
    }
    return edits;
  }

  private boolean containsHash(RevisarTree<T> revisarTree) {
    return revisarTree.getStrLabel().contains("hash");
  }
}
