package br.ufcg.spg.iterator;

import java.util.ArrayList;
import java.util.List;

import br.ufcg.spg.config.TechniqueConfig;
import br.ufcg.spg.database.DependenceDao;
import br.ufcg.spg.database.EditDao;
import br.ufcg.spg.edit.Edit;

public class EditIterator implements Iterator<Edit> {
  private List<Edit> nodes = new ArrayList<>();
  private long lastEdit = -1;
  private int lastIndex = 0;

  @Override
  public boolean hasNext() {
    if (nodes.isEmpty()) {
      getNextNodes();
    }
    return lastIndex < nodes.size();
  }

  @Override
  public Edit next() {
    if (!hasNext()) {
      return null;
    }
    return nodes.get(lastIndex++);
  }

  private void getNextNodes() {
    EditDao editDao = EditDao.getInstance();
    TechniqueConfig config = TechniqueConfig.getInstance();
    if (lastEdit == -1) {
      DependenceDao dao = DependenceDao.getInstance();
      long last = dao.lastDependence().getId();
      if (last == -1) {
        nodes = editDao.getSrcEditsGreatherThan(-1L, config.getMaxEditsToReturn());
        return;
      }
    }
    lastIndex = 0;
    nodes = editDao.getSrcEditsGreatherThan(lastEdit, config.getMaxEditsToReturn());
  }
}
