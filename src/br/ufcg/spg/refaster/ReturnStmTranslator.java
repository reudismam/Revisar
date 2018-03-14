package br.ufcg.spg.refaster;

import br.ufcg.spg.bean.CommitFile;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.diff.DiffCalculator;
import br.ufcg.spg.diff.DiffTreeContext;
import br.ufcg.spg.edit.EditUtils;
import br.ufcg.spg.git.CommitUtils;
import br.ufcg.spg.matcher.AbstractMatchCalculator;
import br.ufcg.spg.matcher.EvaluatorMatchCalculator;
import br.ufcg.spg.matcher.PositionMatchCalculator;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.project.Version;
import br.ufcg.spg.replacement.Replacement;
import br.ufcg.spg.replacement.ReplacementUtils;
import br.ufcg.spg.search.evaluator.IEvaluator;
import br.ufcg.spg.search.evaluator.ValueEvaluator;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.text.edits.TextEdit;

public class ReturnStmTranslator {
  /**
   * Configures return statement.
   * 
   * @param nodei
   *          source node
   * @param nodej
   *          destination node
   * @param srcList
   *          nodes to be replaced
   * @param refasterRule
   *          Refaster rule to be adapted
   * @param ba
   *          before method and after after method
   * @return before and after method with return type configured.
   */
  public static Tuple<MethodDeclaration, MethodDeclaration> config(
      final Tuple<CommitFile, ASTNode> nodei,
      final Tuple<CommitFile, ASTNode> nodej, final List<Replacement<ASTNode>> srcList,
      final List<Replacement<ASTNode>> dstList, final CompilationUnit refasterRule,
      final Tuple<MethodDeclaration, MethodDeclaration> ba, 
      final DiffCalculator diff, final CompilationUnit dstCu, 
      final ProjectInfo  pi) throws BadLocationException, 
      IOException, NoFilepatternException, GitAPIException {
    MethodDeclaration before = ba.getItem1();
    MethodDeclaration after = ba.getItem2();
    final Tuple<List<ASTNode>, List<ASTNode>> bas = ReplacementUtils
        .mapping(srcList, dstList, diff, dstCu);
    final List<ASTNode> befores = bas.getItem1();
    final List<ASTNode> afters = bas.getItem2();
    before = addReturnStatement(nodei, befores, srcList, refasterRule, 
        before, pi.getSrcVersion(), pi);
    after = addReturnStatement(nodej, afters, srcList, refasterRule, after, pi.getDstVersion(), pi);
    return new Tuple<>(before, after);
  }
  
  private static MethodDeclaration addReturnStatement(
      final Tuple<CommitFile, ASTNode> target, final List<ASTNode> nodes,
      final List<Replacement<ASTNode>> targetList, final CompilationUnit refasterRule, 
      MethodDeclaration method, final Version version, final ProjectInfo pi) 
          throws BadLocationException, IOException, NoFilepatternException, GitAPIException {
    final AST ast = refasterRule.getAST();
    final ASTNode template = getTemplate(target, nodes, targetList, version, pi);
    if (template == null) {
      return method;
    }
    ReturnStatement reStatement = (ReturnStatement) method.getBody().statements().get(0);
    reStatement = (ReturnStatement) ASTNode.copySubtree(ast, reStatement);
    final IConfigBody body = ConfigBodyFactory.getConfigBody(target.getItem2(), nodes,  template, method, reStatement, ast);
    method = body.config();
    return method;
  }
  
  /**
   * gets the node edited by replacing the node by the anti-unification
   * variable.
   * 
   * @param file
   *          file
   * @param target
   *          target node
   * @param unified
   *          node anti-unified
   * @param root
   *          root tree
   * @param document
   *          document that will be edited.
   */
  private static ASTNode getTemplate(final Tuple<CommitFile, ASTNode> target, final List<ASTNode> unifieds,
      final List<Replacement<ASTNode>> targetList, final Version version, 
      final ProjectInfo pi) 
          throws BadLocationException, IOException, NoFilepatternException, GitAPIException {
    final String commit = target.getItem1().getCommit();
    CommitUtils.checkoutIfDiffer(commit, pi);
    final String file = target.getItem1().getFilePath();
    final AST ast = AST.newAST(AST.JLS8);
    final List<ASTNode> names = new ArrayList<ASTNode>();
    final List<ASTNode> targets = new ArrayList<>();
    for (int i = 0; i < unifieds.size(); i++) {
      final ASTNode node = unifieds.get(i);
      if (node == null) {
        continue;
      }
      final SimpleName name = ast.newSimpleName("v_" + i);
      final IEvaluator evaluator = new ValueEvaluator(node.toString());
      final AbstractMatchCalculator mcal = new EvaluatorMatchCalculator(evaluator);
      final List<ASTNode> nodes = mcal.getNodes(target.getItem2());
      for (final ASTNode nodek : nodes) {
        names.add(name);
        targets.add(nodek);
      }
    }
    final String [] sources = version.getSource();
    final String [] classpath = version.getClasspath();
    final Document document = rewrite(file, targets, names, sources, classpath);
    final String srcModified = document.get();
    final Tuple<TreeContext, TreeContext> baEdit = EditUtils.beforeAfterCxt(file, srcModified);
    final Tuple<CompilationUnit, CompilationUnit> cunit = EditUtils.beforeAfter(file, srcModified, version);
    final DiffCalculator diff = new DiffTreeContext(baEdit.getItem1(), baEdit.getItem2());
    diff.diff();
    final ITree srcTree = diff.getSrc().getRoot();
    final ITree dstTree = diff.getDst().getRoot();
    AbstractMatchCalculator mcalc = new PositionMatchCalculator(target.getItem2());
    final ITree srcTarget = mcalc.getNode(srcTree);
    final com.github.gumtreediff.matchers.Matcher matcher = diff.getMatcher();
    final ITree dstMatch = matcher.getMappings().getDst(srcTarget);
    if (dstMatch == null) {
      System.out.println("DEBUG: COULD NOT FIND MATCH FOR: " + srcTarget);
      return null;
    }
    mcalc = new PositionMatchCalculator(dstMatch);
    final ITree dstTarget = mcalc.getNode(dstTree);
    mcalc = new PositionMatchCalculator(dstTarget);
    final ASTNode dstAstNode = mcalc.getNode(cunit.getItem2());
    return dstAstNode;
  }
  
  /**
   * gets the node edited by replacing the node by the anti-unification
   * variable.
   * 
   * @param file
   *          - file
   * @param target
   *          - target node
   * @param unified
   *          - node anti-unified
   * @param root
   *          - root tree
   * @param document
   *          - document that will be edited.
   */
  private static Document rewrite(final String file, final List<ASTNode> sources, final List<ASTNode> names, final String[] javaSources,
      final String[] classpath) throws BadLocationException {
    final JParser srcParser = new JParser();
    final CompilationUnit root = srcParser.parseWithDocument(file, javaSources, classpath);
    final Document document = srcParser.getDocument();
    final ASTRewrite rewriter = ASTRewrite.create(root.getAST());
    root.recordModifications();
    // Edit the source code
    for (int i = 0; i < sources.size(); i++) {
      final ASTNode source = sources.get(i);
      final ASTNode name = names.get(i);
      final AbstractMatchCalculator mcalc = new PositionMatchCalculator(source);
      final ASTNode srcTargetNode = mcalc.getNode(root);
      rewriter.replace(srcTargetNode, name, null);
    }
    final TextEdit edit = rewriter.rewriteAST(document, null);
    edit.apply(document);
    return document;
  }

}
