package br.ufcg.spg.comparer;

import com.github.gumtreediff.tree.ITree;

import java.util.Comparator;

public class TreeComparer implements Comparator<ITree> {
  @Override
  public int compare(ITree o1, ITree o2) {
    if (Integer.compare(o1.getPos(), o2.getPos()) != 0) {
      return Integer.compare(o1.getPos(), o2.getPos());
    }
    return -(Integer.compare(o1.getEndPos(), o2.getEndPos()));
  }
}
