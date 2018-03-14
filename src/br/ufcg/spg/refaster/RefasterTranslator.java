package br.ufcg.spg.refaster;

import br.ufcg.spg.bean.CommitFile;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.diff.DiffCalculator;
import br.ufcg.spg.diff.DiffPath;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.git.CommitUtils;
import br.ufcg.spg.matcher.AbstractMatchCalculator;
import br.ufcg.spg.matcher.EvaluatorMatchCalculator;
import br.ufcg.spg.matcher.PositionMatchCalculator;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.project.ProjectAnalyzer;
import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.replacement.Replacement;
import br.ufcg.spg.replacement.ReplacementUtils;
import br.ufcg.spg.search.evaluator.KindEvaluator;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.text.edits.TextEdit;

public class RefasterTranslator {

  /**
   * Translate edit to Refaster rule.
   */
  public static String translate(final Cluster clusteri)
      throws BadLocationException, IOException, JavaModelException, 
      IllegalArgumentException, NoFilepatternException, GitAPIException {
    final JParser refasterRuleParser = new JParser();
    final String refasterFile = "src/br/ufcg/spg/refaster/RefasterTemplate.java";
    final CompilationUnit rule = refasterRuleParser.parseWithDocument(refasterFile);
    final Document document = refasterRuleParser.getDocument();
    // Learn before and after method
    final Tuple<MethodDeclaration, MethodDeclaration> ba = beforeAfter(clusteri, rule);
    // Replace before and after method in Refaster rule.
    final String refaster = replaceBeforeAfter(rule, document, ba);
    return refaster;
  }

  /**
   * Replaces before and after method.
   * 
   * @param rule
   *          Refaster rule
   * @param document
   *          document
   * @param ba
   *          before and after method
   * @return replaced before and after method
   */
  private static String replaceBeforeAfter(final CompilationUnit rule, final Document document,
      final Tuple<MethodDeclaration, MethodDeclaration> ba) throws BadLocationException {
    final TypeDeclaration typeDecl = (TypeDeclaration) rule.types().get(0);
    final ASTRewrite rewrite = ASTRewrite.create(rule.getAST());
    final MethodDeclaration[] methods = typeDecl.getMethods();
    rewrite.replace(methods[0], ba.getItem1(), null);
    rewrite.replace(methods[1], ba.getItem2(), null);
    final TextEdit edits = rewrite.rewriteAST(document, null);
    edits.apply(document);
    final String refaster = document.get();
    return refaster;
  }

  /**
   * Learns before and after method for Refaster rule.
   * 
   * @param srcEdit
   *          before version of the node
   * @param dstEdit
   *          after version of the node
   * @param targetList
   *          list of target to be replaced
   * @param rule
   *          RefasterRule
   * @return before and after method for Refaster rule
   */
  private static Tuple<MethodDeclaration, MethodDeclaration> beforeAfter(
      final Cluster srcCluster, final CompilationUnit rule)
      throws BadLocationException, IOException, NoFilepatternException, GitAPIException {
    final Cluster dstCluster = srcCluster.getDst();
    final Edit srcEdit = srcCluster.getNodes().get(0);
    final Edit dstEdit = dstCluster.getNodes().get(0);
    final ProjectInfo pi = ProjectAnalyzer.project(srcEdit);
    final String commit = dstEdit.getCommit();
    CommitUtils.checkoutIfDiffer(commit, pi);
    final DiffCalculator diff = new DiffPath(srcEdit.getPath(), dstEdit.getPath());
    diff.diff();
    final CompilationUnit dstUnit = JParser.parse(dstEdit.getPath(), pi.getDstVersion());
    final CompilationUnit srcUnit = JParser.parse(srcEdit.getPath(), pi.getSrcVersion());
    AbstractMatchCalculator mcalc = new PositionMatchCalculator(
        srcEdit.getStartPos(), srcEdit.getEndPos());
    final ASTNode nodei = mcalc.getNode(srcUnit);
    mcalc = new PositionMatchCalculator(dstEdit.getStartPos(), dstEdit.getEndPos());
    final ASTNode nodej = mcalc.getNode(dstUnit);
    final Tuple<CommitFile, ASTNode> srcNode = new Tuple<>(
        new CommitFile(srcEdit.getCommit(), srcEdit.getPath()), nodei);
    final Tuple<CommitFile, ASTNode> dstNode = new Tuple<>(
        new CommitFile(dstEdit.getCommit(), dstEdit.getPath()), nodej);
    Tuple<MethodDeclaration, MethodDeclaration> ba = getBeforeAfterMethod(rule);
    final String srcAu = srcCluster.getAu();
    final String dstAu = dstCluster.getAu();    
    final List<Replacement<ASTNode>> src = ReplacementUtils.replacements(srcEdit, srcAu, srcUnit);
    final List<Replacement<ASTNode>> dst = ReplacementUtils.replacements(dstEdit, dstAu, dstUnit);
    // Return statement
    ba = ReturnTypeTranslator.config(nodei, nodej, rule, ba);
    // Add parameters
    ba = ParameterTranslator.config(src, rule, ba);
    // Replace method body
    ba = ReturnStmTranslator.config(srcNode, dstNode, src, dst, rule, ba, diff, dstUnit, pi);
    return ba;
  }
  
  /**
   * Gets before and after method.
   * 
   * @param refasterRule
   *          Refaster rule.
   * @return before and after method
   */
  private static Tuple<MethodDeclaration, MethodDeclaration> getBeforeAfterMethod(
      final CompilationUnit refasterRule) {
    final KindEvaluator evaluator = new KindEvaluator(ASTNode.METHOD_DECLARATION);
    final AbstractMatchCalculator mcal = new EvaluatorMatchCalculator(evaluator);
    final List<ASTNode> nodes = mcal.getNodes(refasterRule);
    final MethodDeclaration before = (MethodDeclaration) nodes.get(0);
    final MethodDeclaration after = (MethodDeclaration) nodes.get(1);
    final Tuple<MethodDeclaration, MethodDeclaration> ba = new Tuple<>(before, after);
    return ba;
  }
}
