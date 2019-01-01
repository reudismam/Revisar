package br.ufcg.spg.edit;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.converter.ConverterHelper;
import br.ufcg.spg.ml.editoperation.DeleteNode;
import br.ufcg.spg.ml.editoperation.EditNode;
import br.ufcg.spg.ml.editoperation.InsertNode;
import br.ufcg.spg.ml.editoperation.MoveNode;
import br.ufcg.spg.ml.editoperation.UpdateNode;
import br.ufcg.spg.tree.BFSWalker;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.TreeTraversal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.tree.TreeNode;

public class EditScriptGenerator<T> {
  /**
   * Create an edit script.
   * 
   * @param t1
   *          Source tree
   * @param t2
   *          Target tree
   * @param m
   *          Mapping between source and target tree nodes
   */
  public List<EditNode<T>> editScript(
      RevisarTree<T> t1, RevisarTree<T> t2, Map<RevisarTree<T>, RevisarTree<T>> m) {
    List<EditNode<T>> editScript = new ArrayList<>();
    List<RevisarTree<T>> bfs = BFSWalker.breadFirstSearch(t2);
    for (RevisarTree<T> x : bfs) {
      // Combines the update, insert, align, and move phases
      RevisarTree<T> y = x.getParent();
      RevisarTree<T> z = findNode(m, y);
      RevisarTree<T> w = findNode(m, x);
      if (w == null) {
        int k = findPos(x, m);
        RevisarTree<T> xnode = new RevisarTree<T>(x.getValue(), x.getLabel());
        EditNode<T> insert = new InsertNode<T>(xnode, z, k);
        z.addChild(xnode, k - 1);
        m.put(xnode, x);
        editScript.add(insert);
      } else { // x has a partner in M
        RevisarTree<T> v = w.getParent();
        if (w.getChildren().isEmpty() && x.getChildren().isEmpty() 
            && !w.toString().equals(x.toString())) {
          // int index = v.Children.TakeWhile(item => !item.Equals(w)).Count();
          final EditNode<T> update = new UpdateNode<T>(w, x, z, y);
          int index = v.getChildren().indexOf(w); 
          v.removeChild(index);
          RevisarTree<T> xnode = new RevisarTree<T>(x.getValue(), x.getLabel());
          v.addChild(xnode, index);
          m.put(xnode, x);
          m.remove(w);
          w = xnode;
          editScript.add(update);
        }
        if (z != null && findNode(m, y, v) == null) {
          int k = findPos(x, m);
          RevisarTree<T> znode = ConverterHelper.makeACopy(z);
          RevisarTree<T> wnode = ConverterHelper.makeACopy(w);
          EditNode<T> move = new MoveNode<T>(wnode, znode, k);
          move.setPreviousParent(w.getParent());
          z.addChild(w, k - 1);
          int index = v.getChildren().indexOf(w);
          // int index = v.Children.TakeWhile(
          //item => !item.Equals(w)).Count();
          v.removeChild(index);
          editScript.add(move);
        }
      }
      // AlignChildren(x, w);
    }
    TreeTraversal<T> traversal = new TreeTraversal<T>();
    List<RevisarTree<T>> nodes = traversal.postOrderTraversal(t1); // the delete phase
    for (int i = 0; i < nodes.size(); i++) {
      RevisarTree<T> w = nodes.get(i);
      if (!m.containsKey(w)) {
        EditNode<T> delete = new DeleteNode<T>(w);
        RevisarTree<T> v = delete.getParent();
        int index = v.getChildren().indexOf(w);
        // int index = v.Children.TakeWhile(item => !item.Equals(w)).Count();
        delete.setK(index);
        v.removeChild(index);
        editScript.add(delete);
      }
    }
    for (EditNode<T> s : editScript) {
      if (s instanceof MoveNode<?>) {
        continue;
      }
      RevisarTree<T> t1copy = ConverterHelper.makeACopy(s.getT1Node());
      t1copy.setChildren(new ArrayList<RevisarTree<T>>());
      RevisarTree<T> parentcopy = ConverterHelper.makeACopy(s.getParent());
      parentcopy.setChildren(new ArrayList<RevisarTree<T>>());
      s.setT1Node(t1copy);
      s.setParent(parentcopy);
    }
    for (int i = 0; i < editScript.size(); i++) {
      EditNode<T> v = editScript.get(i);
      if (v instanceof InsertNode<?>) {
        RevisarTree<T> xnode = new RevisarTree<T>(
            v.getT1Node().getValue(), v.getT1Node().getLabel());
        RevisarTree<T> znode = new RevisarTree<T>(
            v.getParent().getValue(), v.getParent().getLabel());
        EditNode<T> insert = new InsertNode<T>(xnode, znode, v.getK());
        editScript.set(i, insert);
      }
    }
    List<Tuple<EditNode<T>, List<EditNode<T>>>> removes = new ArrayList<>();
    for (EditNode<T> v : editScript) {
      if (v instanceof MoveNode<?>) {
        EditNode<T> move = (MoveNode<T>) v;
        RevisarTree<T> t1copy = ConverterHelper.makeACopy(v.getT1Node());
        RevisarTree<T> parentcopy = ConverterHelper.makeACopy(v.getParent());
        RevisarTree<T> previousParentCopy = ConverterHelper.makeACopy(move.getPreviousParent());
        EditNode<T> insert = new InsertNode<T>(t1copy, parentcopy, v.getK());
        EditNode<T> delete = new DeleteNode<T>(ConverterHelper.makeACopy(t1copy));
        delete.setParent(previousParentCopy);
        List<EditNode<T>> list = new ArrayList<>();
        list.add(delete);
        list.add(insert);
        removes.add(new Tuple<>(v, list));
      }
    }
    for (Tuple<EditNode<T>, List<EditNode<T>>> v : removes) {
      int index = editScript.indexOf(v.getItem1());
      editScript.addAll(index, v.getItem2());
      editScript.remove(v.getItem2());
    }
    return editScript;
  }

  private RevisarTree<T> findNode(Map<RevisarTree<T>, RevisarTree<T>> m,
      RevisarTree<T> y, RevisarTree<T> v) {
    Optional<Entry<RevisarTree<T>, RevisarTree<T>>> node = m.entrySet().stream()
        .filter(o -> o.getValue().equals(y) && o.getKey().equals(v)).findFirst();
    if (node.isPresent()) {
      return node.get().getKey();
    }
    return null;
  }

  private RevisarTree<T> findNode(Map<RevisarTree<T>, RevisarTree<T>> m, RevisarTree<T> y) {
    Optional<Entry<RevisarTree<T>, RevisarTree<T>>> zs = m.entrySet().stream().filter(
        o -> o.getValue().equals(y)).findFirst();
    if (zs.isPresent()) {
      RevisarTree<T> z = zs.get().getKey();
      return z;
    }
    return null;
  }

  /**
   * Find the index in which the edit operations will be executed.
   * 
   * @param x
   *          Node in t2
   * @param m
   *          Mapping
   * @return Index to be updated
   */
  private int findPos(RevisarTree<T> x, 
      Map<RevisarTree<T>, RevisarTree<T>> m) {
    RevisarTree<T> y = x.getParent();
    RevisarTree<T> firstChild = y.getChildren().get(0);
    if (firstChild.equals(x)) {
      return 1;
    }
    RevisarTree<T> v = null;
    for (RevisarTree<T> c : y.getChildren()) {
      if (c.equals(x)) {
        break;
      }
      v = c;
    }
    RevisarTree<T> n = v;
    RevisarTree<T> u = m.entrySet().stream().filter(
        o -> o.getValue().equals(n)).findFirst().get().getKey();
    int count = 1;
    List<RevisarTree<T>> children = u.getParent().getChildren();
    for (RevisarTree<T> c : children) {
      if (c.equals(u)) {
        return count + 1;
      }
      count++;
    }
    return -1;
  }
}
