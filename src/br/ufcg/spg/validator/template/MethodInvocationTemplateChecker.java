package br.ufcg.spg.validator.template;

import br.ufcg.spg.analyzer.util.AnalyzerUtil;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeParser;
import br.ufcg.spg.tree.RevisarTreeUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;

/**
 * Rule that check for valid method invocation.
 */
public class MethodInvocationTemplateChecker implements ITemplateChecker {
  private List<Edit> nodes;

  /**
   * Constructor.
   */
  public MethodInvocationTemplateChecker(final List<Edit> nodes) {
    this.nodes = nodes;
  }
  
  @Override
  public boolean checkIsValidUnification() {
    try {
      if (!isRootMethodInvocation(nodes.get(0))) {
        return true;
      }
      return isSameMethodNameTemplate(nodes);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Verifies if the name of the method is the same.
   * @return true if the name of the method is the same
   */
  private boolean isSameMethodNameTemplate(final List<Edit> srcEdits) 
      throws IOException, MissingObjectException, IncorrectObjectTypeException,
      AmbiguousObjectException, NoFilepatternException, GitAPIException, ExecutionException {
    String methodName = null;
    for (final Edit srcEdit : srcEdits) {
      if (!isRootMethodInvocation(srcEdit)) {
        return true;
      }
      final String template = srcEdit.getTemplate();
      final RevisarTree<String> tree = RevisarTreeParser.parser(template);
      //The name of the method is the second children on the children list.
      final String mname = EquationUtils.convertToEq(tree.getChildren().get(0));
      if (methodName == null) {
        methodName = mname;
      }
      if (!methodName.equals(mname)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Verifies if the root of the tree is a method invocation.
   * @return true if the root of the tree is a method invocation.
   */ 
  private boolean isRootMethodInvocation(final Edit edit) {
    final String template = edit.getTemplate();
    final String root = RevisarTreeUtils.root(template);
    return root.equals(AnalyzerUtil.getLabel(ASTNode.METHOD_INVOCATION));
  }
}
