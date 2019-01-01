package br.ufcg.spg.component;

import br.ufcg.spg.ml.editoperation.DeleteNode;
import br.ufcg.spg.ml.editoperation.EditNode;
import br.ufcg.spg.ml.editoperation.InsertNode;
import br.ufcg.spg.ml.editoperation.MoveNode;
import br.ufcg.spg.ml.editoperation.UpdateNode;
import br.ufcg.spg.tree.RevisarTree;

import java.util.List;

public class FullConnectedEditNode<T> implements ConnectionStrategy {

  private List<EditNode<T>> script;

  public FullConnectedEditNode(final List<EditNode<T>> script) {
    this.script = script;
  }

  /**
   * Verifies whether two nodes are connected.
   */
  public boolean isConnected(final int indexI, final int indexJ) {
    final EditNode<T> editi = script.get(indexI);
    RevisarTree<T> parenti = null;
    RevisarTree<T> parentj = null;
    if (editi instanceof InsertNode<?>) {
      final InsertNode<T> insert = (InsertNode<T>) editi;
      parenti = insert.getParent();
    }
    if (editi instanceof MoveNode<?>) {
      final MoveNode<T> move = (MoveNode<T>) editi;
      parenti = move.getParent();
      return false;
    }
    if (editi instanceof DeleteNode<?>) {
      final DeleteNode<T> delete = (DeleteNode<T>) editi;
      parenti = delete.getT1Node().getParent();
    }
    if (editi instanceof UpdateNode<?>) {
      final UpdateNode<T> update = (UpdateNode<T>) editi;
      parenti = update.getT1Node().getParent();
    }
    final EditNode<T> editj = script.get(indexJ);
    if (editj instanceof InsertNode<?>) {
      final InsertNode<T> insert = (InsertNode<T>) editj;
      parentj = insert.getParent();
    }
    if (editj instanceof MoveNode<?>) {
      final MoveNode<T> move = (MoveNode<T>) editj;
      parentj = move.getParent();
    }
    if (editj instanceof DeleteNode<?>) {
      final DeleteNode<T> delete = (DeleteNode<T>) editj;
      parentj = delete.getT1Node().getParent();
    }
    if (editj instanceof UpdateNode) {
      final UpdateNode<T> update = (UpdateNode<T>) editj;  
      parentj = update.getT1Node().getParent();
    }
    if (parenti != null && parentj != null) {
      // The nodes have the same parent then they are connected
      final boolean isParent = parenti.equals(parentj);
      if (isParent && parenti.getParent() != null) {
        return true;
      }
    }
    // In a sequence of inserts. The node from edit i will be equal to
    // the parent of edit j.
    RevisarTree<T> node = editi.getT1Node();
    return parentj != null && parentj.equals(node);
  }
}