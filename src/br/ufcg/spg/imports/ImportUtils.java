package br.ufcg.spg.imports;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.comparer.ActionComparer;
import br.ufcg.spg.component.ConnectedComponentManager;
import br.ufcg.spg.diff.DiffCalculator;
import br.ufcg.spg.diff.DiffUtils;
import br.ufcg.spg.edit.Edit;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.ITree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;

public class ImportUtils {
  
  /**
   * Gets import statements.
   * @param srcEdit source edit
   * @return import statements
   */
  public static List<Tuple<String,  ITree>> imports(Edit srcEdit) 
      throws MissingObjectException, IncorrectObjectTypeException,
      AmbiguousObjectException, NoFilepatternException, IOException, 
      GitAPIException, ExecutionException {
    Edit dstEdit = srcEdit.getDst();
    DiffCalculator diff = DiffUtils.diff(srcEdit, dstEdit);
    List<Action> actions = diff.diff();
    List<Tuple<String, ITree>> imports = new ArrayList<>();
    if (actions.isEmpty()) {
      return imports;
    }
    ConnectedComponentManager con = new ConnectedComponentManager();
    List<List<Action>> actionList = con.connectedComponents(actions);
    // Comparer
    Comparator<Action> actionComparer = new ActionComparer();
    for (List<Action> list : actionList) {
      Collections.sort(list, actionComparer);
      Action first = list.get(0);
      if (first instanceof Insert || first instanceof Move || first instanceof Update) {
        String pretty = first.getNode().toPrettyString(diff.getSrc());
        if (pretty.equals("ImportDeclaration")) {
          Tuple<String, ITree> ituple = new Tuple<>(srcEdit.getPath(), first.getNode());
          imports.add(ituple);
        } 
      } 
    }
    return imports;
  }
}
