package br.ufcg.spg.antiunification;

import br.ufcg.spg.antiunification.substitution.HoleWithSubstutings;
import br.ufcg.spg.config.TechniqueConfig;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.KindNodeMatcher;
import br.ufcg.spg.matcher.LargerThanMatcher;
import br.ufcg.spg.node.util.ASTNodeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;

public final class AntiUnifierUtils {
  
  private static final Logger logger = LogManager.getLogger(AntiUnifierUtils.class.getName());
  
  public static final String LARGER = "LARGER()";
  
  private AntiUnifierUtils(){
  }
  
  /**
   * Computes the maximum context for a given list of trees.
   * 
   * @param trees
   *          List of trees
   * @param unify
   *          force the algorithm to unify nodes
   * @return Ant-unification
   */
  private static AntiUnifier maxContext(final ASTNode first, final ASTNode second, 
      final ASTNode firstUpper, final ASTNode secondUpper, final boolean unify)
      throws IOException {
    final List<ASTNode> trees = Arrays.asList(first, second); 
    if (!allSameKind(trees)) {
      return createAntiUnification(first, second, null, unify);
    }
    IMatcher<ASTNode> evaluator = new KindNodeMatcher(ASTNode.METHOD_DECLARATION);
    if (isSome(trees, evaluator)) {
      return createAntiUnification(first, second, ASTNodeUtils.getLabel(
          ASTNode.METHOD_DECLARATION), unify);
    }
    evaluator = new LargerThanMatcher(1000);
    if (isSome(trees, evaluator)) {
      return new AntiUnifier(LARGER);
    }
    evaluator = new KindNodeMatcher(ASTNode.FIELD_DECLARATION);
    if (isSome(trees, evaluator)) {
      return createAntiUnification(first, second, ASTNodeUtils.getLabel(
          ASTNode.FIELD_DECLARATION), unify);
    }
    List<ASTNode> upperNodes = Arrays.asList(firstUpper, secondUpper);
    if (someIncludeUpper(trees, upperNodes)) {
      return createAntiUnification(first, second, ASTNodeUtils.getLabel(
          first.getNodeType()), unify);
    }
    AntiUnifier au = createAntiUnification(first, second, ASTNodeUtils.getLabel(
        first.getNodeType()), unify);
    final ASTNode parentFirst = first.getParent();
    final ASTNode parentSecond = second.getParent();
    if (allSameKind(Arrays.asList(parentFirst, parentSecond))) {
      final List<List<ASTNode>> left = getLeftSiblings(trees);
      final List<List<ASTNode>> right = getRightSiblings(trees);
      final AntiUnifier auLeft = antiUnify(left);
      final AntiUnifier auRight = antiUnify(right);
      final AntiUnifier root = maxContext(
          parentFirst, parentSecond, firstUpper, secondUpper, false);
      if (root.getValue().getUnifier().equals(LARGER)) {
        return root;
      }
      root.addChildren(auLeft, au, auRight);
    }
    return au;
  }

  private static AntiUnifier createAntiUnification(final ASTNode first, final ASTNode second, 
      String label, final boolean unify) {
    if (unify) {
      return antiUnify(first, second);
    }
    if (label == null) {
      return new AntiUnifier();
    }
    return new AntiUnifier(label);
  }

  /**
   * Learn unification template.
   * @param first
   *          first target node
   * @param second
   *          second target node
   * @return unification template
   */
  public static AntiUnifier template(final ASTNode first, final ASTNode second, 
      final ASTNode fixedFirst, final ASTNode fixedSecond)
      throws IOException {
    // compute template
    final AntiUnifier template = AntiUnifierUtils.maxContext(first, second, fixedFirst, 
        fixedSecond, true);
    final AntiUnifier root = AntiUnifierUtils.getRoot(template);
    if (root == null) {
      logger.trace("A transformation could not be learned!");
    }
    return root;
  }

  /**
   * Gets left siblings of a list of nodes.
   */
  private static List<List<ASTNode>> getLeftSiblings(final List<ASTNode> trees) {
    final List<List<ASTNode>> left = new ArrayList<>();
    for (final ASTNode tree : trees) {
      final ASTNode parent = tree.getParent();
      final List<Object> children = ASTNodeUtils.getChildren(parent);
      final List<ASTNode> normalizedChildren = ASTNodeUtils.normalize(children);
      final int index = normalizedChildren.indexOf(tree);
      final List<ASTNode> subList = normalizedChildren.subList(0, index);
      if (!subList.isEmpty()) {
        left.add(subList);
      }
    }
    return left;
  }

  /**
   * Gets right siblings of a list of nodes.
   */
  private static List<List<ASTNode>> getRightSiblings(final List<ASTNode> trees) {
    final List<List<ASTNode>> right = new ArrayList<>();
    for (final ASTNode tree : trees) {
      final ASTNode parent = tree.getParent();
      final List<Object> children = ASTNodeUtils.getChildren(parent);
      final List<ASTNode> normalizedChildren = ASTNodeUtils.normalize(children);
      final int index = normalizedChildren.indexOf(tree);
      final List<ASTNode> subList = normalizedChildren.subList(index + 1, 
          normalizedChildren.size());
      if (!subList.isEmpty()) {
        right.add(subList);
      }
    }
    return right;
  }

  /**
   * Verifies if all the trees are from the same type.
   * @param trees trees to be analyzed
   * @return true if all the trees are from the same type.
   */
  public static boolean allSameKind(final List<ASTNode> trees) {
    if (trees.isEmpty()) {
      throw new UnsupportedOperationException("Trees could not be null.");
    }
    final ASTNode first = trees.get(0);
    for (final ASTNode tree : trees) {
      if (tree.getNodeType() != first.getNodeType()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Verifies if some tree is method declaration.
   * @param trees list of trees to be analyzed.
   * @return true if some tree is method declaration
   */
  public static boolean isSome(final List<ASTNode> trees, final IMatcher<ASTNode> eval) {
    if (trees.isEmpty()) {
      throw new UnsupportedOperationException("Trees could not be null.");
    }
    for (final ASTNode tree : trees) {
      if (eval.evaluate(tree)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Verifies true if any tree contains fixed context.
   * @param trees trees
   * @param upperNodes upper trees.
   * @return true if any tree contains fixed context
   */
  public static boolean someIncludeUpper(final List<ASTNode> trees, 
      final List<ASTNode> upperNodes) {
    if (trees.isEmpty()) {
      throw new UnsupportedOperationException("Trees could not be null.");
    }
    for (int i = 0; i < trees.size(); i++) {
      final ASTNode tree = trees.get(i);
      final ASTNode upper = upperNodes.get(i);
      if (tree.equals(upper)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Anti-unify trees.
   * @param trees trees
   * @return anti-unification
   */
  public static AntiUnifier antiUnify(final List<List<ASTNode>> trees) {
    if (trees.isEmpty()) {
      return new AntiUnifier();
    }
    if (trees.size() < 2) {
      return new AntiUnifier("#");
    }
    final List<ASTNode> first = trees.get(0);
    final List<ASTNode> second = trees.get(1);
    if (first.isEmpty() && second.isEmpty()) {
      return new AntiUnifier("#");
    }
    if (first.isEmpty() || second.isEmpty()) {
      return new AntiUnifier();
    }
    final String eq1 = EquationUtils.convertToEquation(trees.get(0));
    final String eq2 = EquationUtils.convertToEquation(trees.get(1));
    return antiUnify(eq1, eq2);
  }

  /**
   * Anti-unifies two nodes.
   * 
   * @param first
   *          node
   * @param second
   *          node
   * @return anti-unification for nodes
   */
  public static AntiUnifier antiUnify(final ASTNode first, final ASTNode second) {
    final String eq1 = EquationUtils.convertToAuEq(first);
    final String eq2 = EquationUtils.convertToAuEq(second);
    try {
      return antiUnify(eq1, eq2);
    } catch (final Exception e) {
      throw new RuntimeException("Error while computing equations");
    }
  }

  /**
   * Anti-unifies two nodes.
   * 
   * @param eq1
   *          node
   * @param eq2
   *          node
   * @return anti-unification for nodes
   */
  public static AntiUnifier antiUnify(final String eq1, final String eq2) {
    if (eq1.length() > 1000 || eq2.length() > 1000) {
      return new AntiUnifier(LARGER);
    }
    tryUnify(eq1, eq2);
    if (unification == null) {
      return new AntiUnifier(LARGER);
    }
    return new AntiUnifier(unification);
  }

  private static AntiUnificationData unification = null;
  
  /**
   * Try to unify eq1 and eq2.
   * @param eq1 equation one
   * @param eq2 equation two
   */
  public static void tryUnify(final String eq1, final String eq2) {
    final ExecutorService executor = Executors.newFixedThreadPool(4);   
    final Future<?> future = executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          unification = TechniqueConfig.getInstance().getAuAlgorithm().unify(eq1, eq2);
        } catch (final Exception e) {
          logger.error(e.getStackTrace());
        }
      }
    });
    executor.shutdown();
    try {
      future.get(2, TimeUnit.SECONDS);
    } catch (final InterruptedException e) {
      logger.trace("job was interrupted");
      unification = null;
    } catch (final ExecutionException e) {
      logger.trace("caught exception: " + e.getCause());
      unification = null;
    } catch (final TimeoutException e) {
      future.cancel(true);
      unification = null;
      logger.trace("timeout");
    }
    try {
      if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Get the root of an anti-unification algorithm.
   * 
   * @param au
   *          - anti-unification
   * @return root of the anti-unification
   */
  public static AntiUnifier getRoot(final AntiUnifier au) {
    AntiUnifier root = au.getParent();
    AntiUnifier previous = au;
    while (root != null) {
      previous = root;
      root = root.getParent();
    }
    return previous;
  }

  /**
   * Computes anti-unification.
   * 
   * @param au
   *          first anti-unification
   * @param clusterAu
   *          second anti-unification
   * @return anti-unification
   */
  public static AntiUnifier computeUnification(final AntiUnifier au, final AntiUnifier clusterAu) {
    final String eqCluster = EquationUtils.convertToEquation(clusterAu);
    final String eqOther = EquationUtils.convertToEquation(au);
    return antiUnify(eqCluster, eqOther);
  }
  
  /**
   * Gets unifier matching.
   **/
  public static Map<String, String> getUnifierMatching(final String cluterTemplate,
      final String template) {
    final AntiUnifier unifier = AntiUnifierUtils.antiUnify(cluterTemplate, template);
    final List<HoleWithSubstutings> dstVariables = unifier.getValue().getVariables();
    final Map<String, String> unifierMatching = new HashMap<>();
    for (final HoleWithSubstutings variable : dstVariables) {
      final String strRight = AntiUnifierUtils.removeEnclosingParenthesis(
          variable.getRightSubstuting());
      final String strLeft = AntiUnifierUtils.removeEnclosingParenthesis(
          variable.getLeftSubstuting());
      unifierMatching.put(strLeft, strRight);
    }
    return unifierMatching;
  }

  /**
   * Gets substutings.
   **/
  public static Set<String> getSubstitutings(final Edit edit, final String au) {
    final Set<String> holes = new HashSet<>();
    final AntiUnifier unifier = AntiUnifierUtils.antiUnify(au, edit.getPlainTemplate());
    final List<HoleWithSubstutings> variables = unifier.getValue().getVariables();
    for (final HoleWithSubstutings variable : variables) {
      final String str = AntiUnifierUtils.removeEnclosingParenthesis(variable.getRightSubstuting());
      holes.add(str);
    }
    return holes;
  }
  
  /**
   * Remove parenthesis.
   * 
   * @param variable
   *          hedge variable
   * @return string without parenthesis
   */
  public static String removeEnclosingParenthesis(final String variable) {
    final String str = variable.trim();
    final boolean startWithParen = str.startsWith("(");
    final boolean endWithParen = str.endsWith(")");
    if (!str.isEmpty() && startWithParen && endWithParen) {
      return str.substring(1, str.length() - 1);
    }
    return str;
  }
}
