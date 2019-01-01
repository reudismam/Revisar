package br.ufcg.spg.component;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.ITree;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public class FullConnectedGumTree implements ConnectionStrategy {

  private List<Action> script;

  public FullConnectedGumTree(final List<Action> script) {
    this.script = script;
  }

  /**
   * Verifies whether two nodes are connected.
   */
  public boolean isConnected(final int indexI, final int indexJ) {
    final Action editi = script.get(indexI);
    ITree parenti = null;
    ITree parentj = null;

    if (editi instanceof Insert) {
      final Insert insert = (Insert) editi;
      parenti = insert.getParent();
    }
    if (editi instanceof Move) {
      final Move move = (Move) editi;
      parenti = move.getParent();
      return false;
    }
    if (editi instanceof Delete) {
      final Delete delete = (Delete) editi;
      parenti = delete.getNode().getParent();
    }
    if (editi instanceof Update) {
      final Update update = (Update) editi;
      parenti = update.getNode().getParent();
    }
    final Action editj = script.get(indexJ);
    if (editj instanceof Insert) {
      final Insert insert = (Insert) editj;
      parentj = insert.getParent();
    }
    if (editj instanceof Move) {
      final Move move = (Move) editj;
      parentj = move.getParent();
    }
    if (editj instanceof Delete) {
      final Delete delete = (Delete) editj;
      parentj = delete.getNode().getParent();
    }
    if (editj instanceof Update) {
      final Update update = (Update) editj;  
      parentj = update.getNode().getParent();
    }
    if (parenti != null && parentj != null) {
      // The nodes have the same parent then they are connected
      final int compilationUnitId = ASTNode.COMPILATION_UNIT;
      final boolean isParent = parenti.equals(parentj);
      if (isParent && parenti.getType() != compilationUnitId) {
        return true;
      }
    }
    // In a sequence of inserts. The node from edit i will be equal to
    // the parent of edit j.
    return parentj != null && parentj.equals(editi.getNode());
  }
}