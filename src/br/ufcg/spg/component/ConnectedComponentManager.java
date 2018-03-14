package br.ufcg.spg.component;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.ITree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Compute connected components.
 * 
 * @author SPG-04
 *
 */
public class ConnectedComponentManager {

  /**
   * Visited edit operations. Required to compute connected components (DFS
   * implementation).
   */
  private static HashMap<Action, Integer> _visited;

  /**
   * Edit operations graph.
   */
  private static HashMap<Action, List<Action>> graph;

  private FullConnected connectionComparer;

  /**
   * Compute connected components.
   */
  private static List<List<Action>> computeConnectedComponents(final List<Action> editOperations) {
    _visited = new HashMap<Action, Integer>();
    int i = 0;
    final HashMap<Integer, List<Action>> dic = new HashMap<Integer, List<Action>>();
    for (final Action edit : editOperations) {
      final Action t = edit;
      if (!_visited.containsKey(t)) {
        dic.put(i, new ArrayList<Action>());
        depthFirstSearch(edit, i++);
      }
    }

    for (final Action edit : editOperations) {
      final Action t = edit;
      final int cc = _visited.get(t);
      dic.get(cc).add(edit);
    }
    final List<List<Action>> ccs = new ArrayList<List<Action>>(dic.values());
    return ccs;
  }

  /**
   * Depth first search.
   * 
   * @param editOperation edit operation
   * @param i index of he edit operation
   */
  private static void depthFirstSearch(final Action editOperation, final int i) {
    final Action t = editOperation;
    _visited.put(t, i);
    for (final Action edit : graph.get(t)) {
      final Action te = edit;
      if (!_visited.containsKey(te)) {
        depthFirstSearch(edit, i);
      }
    }
  }

  /**
   * Computes connected components.
   * @param editOperations edit operations.
   * @return connected components
   */
  public List<List<Action>> connectedComponents(final List<Action> editOperations) {
    connectionComparer = new FullConnected(editOperations);
    buildDigraph(editOperations);
    final List<List<Action>> ccs = computeConnectedComponents(editOperations);
    return ccs;
  }

  /**
   * Build a directed graph of the transformation. An edition i is connected to
   * edition j if edit j depends that edit i insert a node in the tree.
   * 
   * @param script list of actions
   */
  private void buildDigraph(final List<Action> script) {
    graph = new HashMap<Action, List<Action>>();
    for (final Action edit : script) {
      final Action t = edit;
      graph.put(t, new ArrayList<Action>());
    }
    for (int i = 0; i < script.size(); i++) {
      final Action editI = script.get(i);
      final Action ti = editI;

      for (int j = 0; j < script.size(); j++) {
        if (i == j) {
          continue;
        }
        final Action editJ = script.get(j);
        final Action tj = editJ;

        if (connectionComparer.isConnected(i, j)) {
          graph.get(ti).add(editJ);
          graph.get(tj).add(editI);
        }
      }
    }
  }

  private class FullConnected {

    public List<Action> script;

    public FullConnected(final List<Action> script) {
      this.script = script;
    }

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
      if (parentj != null && parentj.equals(editi.getNode())) {
        return true;
      }

      return false;
    }
  }
}
