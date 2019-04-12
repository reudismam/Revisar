package br.ufcg.spg.comparer;

import com.github.gumtreediff.actions.model.Action;

import java.util.Comparator;

public class ActionComparer implements Comparator<Action> {
  @Override
  public int compare(final Action o1, final Action o2) {
    if (o1.getNode().getPos() < o2.getNode().getPos()) {
      return -1;
    }
    if (o1.getNode().getPos() > o2.getNode().getPos()) {
      return 1;
    }
    if (o1.getNode().getEndPos() < o2.getNode().getEndPos()) {
      return 1;
    }
    if (o1.getNode().getEndPos() > o2.getNode().getEndPos()) {
      return -1;
    }
    return 0;
  }
}
