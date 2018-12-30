package br.ufcg.spg.tree;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class BFSWalker {
  /**
   * Breadth First Search traversal.
   * 
   * @param u
   *          Node to be traversed
   */
  public static <T> List<RevisarTree<T>> breadFirstSearch(RevisarTree<T> u) {
    List<RevisarTree<T>> result = new ArrayList<>();
    Map<RevisarTree<T>, Integer> dist = new Hashtable<>();
    dist.put(u, 0);
    Queue<RevisarTree<T>> q = new LinkedList<>();
    q.add(u);
    while (!q.isEmpty()) {
      RevisarTree<T> v = q.poll();
      for (RevisarTree<T> c : v.getChildren()) {
        if (!dist.containsKey(c)) {
          dist.put(c, dist.get(v) + 1);
          result.add(c);
          q.add(c);
        }
      }
    }
    return result;
  }
}
