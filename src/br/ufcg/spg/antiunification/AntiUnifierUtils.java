package br.ufcg.spg.antiunification;

import at.jku.risc.stout.urauc.algo.AlignFnc;
import at.jku.risc.stout.urauc.algo.AlignFncLAA;
import at.jku.risc.stout.urauc.algo.AntiUnifyProblem;
import at.jku.risc.stout.urauc.algo.AntiUnifyProblem.VariableWithHedges;
import at.jku.risc.stout.urauc.algo.DebugLevel;
import at.jku.risc.stout.urauc.algo.JustificationException;
import at.jku.risc.stout.urauc.data.EquationSystem;
import at.jku.risc.stout.urauc.data.Hedge;
import at.jku.risc.stout.urauc.data.InputParser;
import at.jku.risc.stout.urauc.util.ControlledException;
import br.ufcg.spg.cluster.ClusterUnifier;
import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.node.util.ASTNodeUtils;
import br.ufcg.spg.search.evaluator.IEvaluator;
import br.ufcg.spg.search.evaluator.KindEvaluator;
import br.ufcg.spg.search.evaluator.SizeEvaluator;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.core.dom.ASTNode;

public final class AntiUnifierUtils {
  
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
      throws JustificationException, IOException, ControlledException {
    List<ASTNode> trees = Arrays.asList(first, second); 
    if (!allSameKind(trees)) {
      return createAntiUnification(first, second, null, unify);
    }
    IEvaluator evaluator = new KindEvaluator(ASTNode.METHOD_DECLARATION);
    if (isSome(trees, evaluator)) {
      return createAntiUnification(first, second, ASTNodeUtils.getLabel(
          ASTNode.METHOD_DECLARATION), unify);
    }
    evaluator = new SizeEvaluator();
    if (isSome(trees, evaluator)) {
      return new AntiUnifier("LARGER()");
    }
    evaluator = new KindEvaluator(ASTNode.FIELD_DECLARATION);
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
      if (root.getValue().getUnifier().equals("LARGER()")) {
        return root;
      }
      root.addChildren(auLeft, au, auRight);
    }
    return au;
  }

  private static AntiUnifier createAntiUnification(final ASTNode first, final ASTNode second, 
      String label, final boolean unify)
      throws IOException, JustificationException, ControlledException {
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
   * 
   * @param first
   *          first target node
   * @param second
   *          second target node
   * @return unification template
   */
  public static AntiUnifier template(final ASTNode first, final ASTNode second, 
      final ASTNode fixedFirst, final ASTNode fixedSecond)
      throws JustificationException, IOException, ControlledException {
    // compute template
    final AntiUnifier template = AntiUnifierUtils.maxContext(first, second, fixedFirst, 
        fixedSecond, true);
    final AntiUnifier root = AntiUnifierUtils.getRoot(template);
    if (root == null) {
      System.out.println("A transformation could not be learned!");
    }
    return root;
  }

  /**
   * Gets left siblings of a list of nodes.
   */
  private static List<List<ASTNode>> getLeftSiblings(final List<ASTNode> trees) {
    final List<List<ASTNode>> left = new ArrayList<List<ASTNode>>();
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
    final List<List<ASTNode>> right = new ArrayList<List<ASTNode>>();
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
   * Verifies if some tree is method declaration
   * @param trees list of trees to be analyzed.
   * @return true if some tree is method declaration
   */
  public static boolean isSome(final List<ASTNode> trees, final IEvaluator eval) {
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
   * Verifies true if any tree contains fixed context
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

  //TODO resolve bug here.
  /**
   * Anti-unify trees.
   * @param trees trees
   * @return anti-unification
   */
  public static AntiUnifier antiUnify(final List<List<ASTNode>> trees)
      throws IOException, JustificationException, ControlledException {
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
    final AntiUnifier au = antiUnify(eq1, eq2);
    return au;
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
  public static AntiUnifier antiUnify(final ASTNode first, final ASTNode second)
      throws IOException, JustificationException, ControlledException {
    final String eq1 = EquationUtils.convertToAuEq(first);
    final String eq2 = EquationUtils.convertToAuEq(second);
    AntiUnifier au = null;
    try {
      au = antiUnify(eq1, eq2);
    } catch (final Exception e) {
      throw new RuntimeException("Error while computing equations");
    }
    return au;
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
  public static AntiUnifier antiUnify(final String eq1, final String eq2) 
      throws JustificationException, ControlledException, IOException {
    if (eq1.length() > 1000 || eq2.length() > 1000) {
      return new AntiUnifier("LARGER()");
    }
    tryUnify(eq1, eq2);
    if (unification == null) {
      return new AntiUnifier("LARGER()");
    }
    final AntiUnifier au = new AntiUnifier(unification);
    return au;
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
          unification = unify(eq1, eq2);
        } catch (final JustificationException e) {
          e.printStackTrace();
        } catch (final ControlledException e) {
          e.printStackTrace();
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    });
    executor.shutdown();
    try {
      future.get(2, TimeUnit.SECONDS);
    } catch (final InterruptedException e) {
      System.out.println("job was interrupted");
      unification = null;
    } catch (final ExecutionException e) {
      System.out.println("caught exception: " + e.getCause());
      unification = null;
    } catch (final TimeoutException e) {
      future.cancel(true);
      unification = null;
      System.out.println("timeout");
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
   * computes the anti-unification of two equations.
   */
  private static AntiUnificationData unify(final String eq1, final String eq2) 
      throws JustificationException, ControlledException, IOException {
    final Reader in1 = new StringReader(eq1);
    final Reader in2 = new StringReader(eq2);
    final boolean iterateAll = true;
    final AlignFnc alFnc = new AlignFncLAA();
    final EquationSystem<AntiUnifyProblem> eqSys = new EquationSystem<AntiUnifyProblem>() {
      @Override
      public AntiUnifyProblem newEquation() {
        return new AntiUnifyProblem();
      }
    };
    new InputParser<AntiUnifyProblem>(eqSys).parseHedgeEquation(in1, in2);
    final AntiUnifierHoles antUnifier = new AntiUnifierHoles(alFnc, eqSys, DebugLevel.SILENT);
    antUnifier.antiUnify(iterateAll, false, System.out);
    return antUnifier.getUnification();
  }
  
  /**
   * Gets the hash_id pair and value.
   * @return mapping
   */
  public static Map<String, String> getUnifierMatching(final String cluterTemplate, 
      final String template) {
    final AntiUnifier unifier = ClusterUnifier.antiUnify(cluterTemplate, template);
    final List<VariableWithHedges> dstVariables = unifier.getValue().getVariables();
    final Map<String, String> unifierMatching = new Hashtable<>();
    for (final VariableWithHedges variable : dstVariables) {
      final String strRight = removeEnclosingParenthesis(variable.getRight());
      final String strLeft = removeEnclosingParenthesis(variable.getLeft());
      unifierMatching.put(strLeft, strRight);
    }
    return unifierMatching;
  }
  
  /**
   * Remove parenthesis.
   * @param variable hedge variable
   * @return string without parenthesis
   */
  private static String removeEnclosingParenthesis(final Hedge variable) {
    final String str = variable.toString().trim();
    final boolean startWithParen = str.startsWith("(");
    final boolean endWithParen = str.endsWith(")");
    if (!str.isEmpty() && startWithParen && endWithParen) {
      return str.substring(1, str.length() - 1);
    }
    return str;
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
}
