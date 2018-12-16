package br.ufcg.spg.emerging;

import br.ufcg.spg.edit.Edit;

import java.util.Comparator;

public class SortbyDate implements Comparator<Edit> {
  public int compare(Edit a, Edit b) {
    return new Long(a.getDate().getTime()).compareTo(b.getDate().getTime());
  }
}
