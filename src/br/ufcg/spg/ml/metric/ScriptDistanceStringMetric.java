package br.ufcg.spg.ml.metric;

import br.ufcg.spg.component.ConnectedComponentManager;
import br.ufcg.spg.component.ConnectionStrategy;
import br.ufcg.spg.component.FullConnectedEditNode;
import br.ufcg.spg.hash.MD5;
import br.ufcg.spg.ml.editoperation.EditNode;
import br.ufcg.spg.ml.editoperation.Script;
import br.ufcg.spg.ml.editoperation.UpdateNode;
import br.ufcg.spg.tree.RevisarTree;
import de.jail.geometry.distancefunctions.PointBasedDistanceFunction;
import de.jail.geometry.schemas.Point;

import java.util.ArrayList;
import java.util.List;

public class ScriptDistanceStringMetric<T> implements PointBasedDistanceFunction {
  @Override
  public double calculate(Point point0, Point point1) {
    @SuppressWarnings("unchecked")
    Script<T> a0 = (Script<T>) point0;
    @SuppressWarnings("unchecked")
    Script<T> a1 = (Script<T>) point1;
    String arg0 = getEdits(a0);
    String arg1 = getEdits(a1);
    double common = Levenshtein.calculate(arg0, arg1);
    if (common == 0.0) {
      return 0.0;
    }
    return (double) common / (double) (Math.max(arg0.length(), arg1.length()));
  }

  private String getEdits(Script<T> arg0) {
    ConnectedComponentManager<EditNode<T>> con0 = new ConnectedComponentManager<>();
    ConnectionStrategy stt0 = new FullConnectedEditNode<>(arg0.getList());
    List<List<EditNode<T>>> a0 = con0.connectedComponents(arg0.getList(), stt0);
    List<List<EditNode<T>>> mean0 = getMeaningfulEdits(a0);
    String result = convertToString(mean0);
    return result;
  }
  
  private String convertToString(List<List<EditNode<T>>> mean0) {
    String result = "";
    for (List<EditNode<T>> edits : mean0) {
      String editStr = "";
      editStr += MD5.getMd5(edits.get(0).getClass().getSimpleName());
      for (EditNode<T> edit : edits) {
        editStr += MD5.getMd5(edit.formatLabel(edit.getT1Node().getStrLabel()));
      }
      result += editStr;
    }
    return result;
  }

  /**
   * Gets meaningful edits.
   */
  private List<List<EditNode<T>>> getMeaningfulEdits(List<List<EditNode<T>>> list) {
    List<List<EditNode<T>>> editsList = new ArrayList<>();
    for (List<EditNode<T>> edits : list) {
      if (edits.size() == 1 && edits.get(0) instanceof UpdateNode<?>) {
        UpdateNode<T> update = (UpdateNode<T>) edits.get(0);
        if (!(containsHash(update.getT1Node()) && containsHash(update.getTo()))) {
          editsList.add(edits);
        }
      } else {
        List<EditNode<T>> keep = new ArrayList<>();
        for (EditNode<T> edit : edits) {
          if (!containsHash(edit.getT1Node())) {
            keep.add(edit);
          }
        }
        if (!edits.isEmpty()) {
          editsList.add(edits);
        }
      }
    }
    return editsList;
  }
  
  public double getEpsilon() {
    return 0.4;
  }

  private boolean containsHash(RevisarTree<T> revisarTree) {
    return revisarTree.getStrLabel().contains("hash");
  }
}
