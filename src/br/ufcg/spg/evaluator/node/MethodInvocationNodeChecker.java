package br.ufcg.spg.evaluator.node;

import br.ufcg.spg.analyzer.util.AnalyzerUtil;
import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.tree.RevisarTreeParser;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;

/**
 * Rule that check for valid method invocation.
 */
public class MethodInvocationNodeChecker implements IValidationNodeRule {

  /**
   * Constructor.
   */
  public MethodInvocationNodeChecker() {
    super();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean checker(final List<ASTNode> nodes) {
    try {
      if (!isRootMethodInvocation(nodes)) {
        return true;
      }
      return isSameMethodName(nodes);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public boolean checkerTemplate(List<String> nodes) {
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
  private boolean isSameMethodName(List<ASTNode> nodes) 
      throws IOException, MissingObjectException, IncorrectObjectTypeException,
      AmbiguousObjectException, NoFilepatternException, GitAPIException, ExecutionException {
    String methodName = null;
    for (final ASTNode node : nodes) {
      if (!(node instanceof MethodInvocation)) {
        return true;
      }
      final MethodInvocation srcMethod = (MethodInvocation) node;
      final String mname = srcMethod.getName().getIdentifier();
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
   * Verifies if the name of the method is the same.
   * @return true if the name of the method is the same
   */
  private boolean isSameMethodNameTemplate(List<String> templates) 
      throws IOException, MissingObjectException, IncorrectObjectTypeException,
      AmbiguousObjectException, NoFilepatternException, GitAPIException, ExecutionException {
    String methodName = null;
    for (final String template : templates) {
      if (!isRootMethodInvocation(template)) {
        return true;
      }
      final RevisarTree<String> tree = RevisarTreeParser.parser(template);
      //The name of the method is the second children on the children list.
      final String mname = EquationUtils.convertToEq(tree.getChildren().get(1));
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
  private boolean isRootMethodInvocation(List<ASTNode> nodes) {
    final ASTNode node = nodes.get(0);
    return node.getNodeType() == ASTNode.METHOD_INVOCATION;
  }
  
  private boolean isRootMethodInvocation(String template) {
    final String root = RevisarTreeUtils.root(template);
    return root.equals(AnalyzerUtil.getLabel(ASTNode.IMPORT_DECLARATION));
  }
}
