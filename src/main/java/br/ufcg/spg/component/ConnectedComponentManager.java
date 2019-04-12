package br.ufcg.spg.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Compute connected components.
 * 
 * @author SPG-04
 *
 */
public class ConnectedComponentManager<T> {

  /**
   * Visited edit operations. Required to compute connected components (DFS
   * implementation).
   */
  private HashMap<T, Integer> visited;

  /**
   * Edit operations graph.
   */
  private HashMap<T, List<T>> graph;

  private ConnectionStrategy connectionComparer;

  /**
   * Compute connected components.
   */
  private List<List<T>> computeConnectedComponents(final List<T> editOperations) {
    visited = new HashMap<>();
    int i = 0;
    final HashMap<Integer, List<T>> dic = new HashMap<>();
    for (final T edit : editOperations) {
      final T t = edit;
      if (!visited.containsKey(t)) {
        dic.put(i, new ArrayList<T>());
        depthFirstSearch(edit, i++);
      }
    }
    for (final T edit : editOperations) {
      final T t = edit;
      final int cc = visited.get(t);
      dic.get(cc).add(edit);
    }
    return new ArrayList<>(dic.values());
  }

  /**
   * Depth first search.
   * 
   * @param editOperation edit operation
   * @param i index of he edit operation
   */
  private void depthFirstSearch(final T editOperation, final int i) {
    final T t = editOperation;
    visited.put(t, i);
    for (final T edit : graph.get(t)) {
      final T te = edit;
      if (!visited.containsKey(te)) {
        depthFirstSearch(edit, i);
      }
    }
  }

  /**
   * Computes connected components.
   * @param editOperations edit operations.
   * @return connected components
   */
  public List<List<T>> connectedComponents(final List<T> editOperations, 
      ConnectionStrategy connectionComparer) {
    this.connectionComparer = connectionComparer;
    buildDigraph(editOperations);
    return computeConnectedComponents(editOperations);
  }

  /**
   * Build a directed graph of the transformation. An edition i is connected to
   * edition j if edit j depends that edit i insert a node in the tree.
   * 
   * @param script list of Ts
   */
  private void buildDigraph(final List<T> script) {
    graph = new HashMap<>();
    for (final T edit : script) {
      final T t = edit;
      graph.put(t, new ArrayList<T>());
    }
    for (int i = 0; i < script.size(); i++) {
      final T editI = script.get(i);
      final T ti = editI;

      for (int j = 0; j < script.size(); j++) {
        if (i == j) {
          continue;
        }
        final T editJ = script.get(j);
        final T tj = editJ;

        if (connectionComparer.isConnected(i, j)) {
          graph.get(ti).add(editJ);
          graph.get(tj).add(editI);
        }
      }
    }
  }
}
