package br.ufcg.spg.refaster;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.diff.DiffCalculator;
import br.ufcg.spg.diff.DiffPath;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.git.CommitUtils;
import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.KindNodeMatcher;
import br.ufcg.spg.matcher.PositionNodeMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.NodeMatchCalculator;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.project.ProjectAnalyzer;
import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.refaster.config.TransformationConfigObject;
import br.ufcg.spg.replacement.Replacement;
import br.ufcg.spg.replacement.ReplacementUtils;
import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.text.edits.TextEdit;

public class RefasterTranslator {
  
  private RefasterTranslator() {
  }

  /**
   * Translate edit to Refaster rule.
   */
  public static String translate(final Cluster clusteri, final Edit srcEdit)
      throws BadLocationException, IOException, GitAPIException {
    final JParser refasterRuleParser = new JParser();
    final String refasterFile = RefasterConstants.RefasterPath;
    final CompilationUnit rule = refasterRuleParser.parseWithDocument(refasterFile);
    final Document document = refasterRuleParser.getDocument();
    // Learn before and after method
    final Tuple<MethodDeclaration, MethodDeclaration> ba = beforeAfter(clusteri, rule, srcEdit);
    // Replace before and after method in Refaster rule.
    return replaceBeforeAfter(rule, document, ba);
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
    return document.get();
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
      final Cluster srcCluster, final CompilationUnit rule, Edit srcEdit)
      throws BadLocationException, IOException, GitAPIException {
    final Cluster dstCluster = srcCluster.getDst();
    //gets only first location, since other locations
    //follow the same template
    final Edit dstEdit = srcEdit.getDst();
    final ProjectInfo pi = checkoutIfDiffer(srcEdit);
    final CompilationUnit dstUnit = JParser.parse(dstEdit.getPath(), pi.getDstVersion());
    final CompilationUnit srcUnit = JParser.parse(srcEdit.getPath(), pi.getSrcVersion());
    IMatcher<ASTNode> srcMatch = new PositionNodeMatcher(srcEdit.getStartPos(), 
        srcEdit.getEndPos());
    MatchCalculator<ASTNode> mcalc = new NodeMatchCalculator(srcMatch);
    final ASTNode srcNode = mcalc.getNode(srcUnit);
    IMatcher<ASTNode> dstMatch = new PositionNodeMatcher(dstEdit.getStartPos(), 
        dstEdit.getEndPos());
    mcalc = new NodeMatchCalculator(dstMatch);
    final ASTNode dstNode = mcalc.getNode(dstUnit);
    Tuple<MethodDeclaration, MethodDeclaration> ba = getBeforeAfterMethod(rule);
    final String srcAu = srcCluster.getAu();
    final String dstAu = dstCluster.getAu();    
    final List<Replacement<ASTNode>> src = ReplacementUtils.replacements(srcEdit, srcAu, srcUnit);
    final List<Replacement<ASTNode>> dst = ReplacementUtils.replacements(dstEdit, dstAu, dstUnit);
    // Return statement
    ba = ReturnTypeTranslator.config(srcNode, dstNode, rule, ba);
    // Add parameters
    ba = ParameterTranslator.config(src, rule, ba);
    // Replace method body
    final DiffCalculator diff = new DiffPath(srcEdit.getPath(), dstEdit.getPath());
    diff.diff();
    String commit = dstEdit.getCommit();
    String srcPath = srcEdit.getPath();
    String dstPath = dstEdit.getPath();
    TransformationConfigObject config = configTransformationObject(
        commit, srcPath, dstPath, rule, pi, 
        dstUnit, srcNode, dstNode, ba, src, dst, diff);
    ba = ReturnStmTranslator.config(config);
    return ba;
  }

  /**
   * Setup transformation configuration object.
   */
  private static TransformationConfigObject configTransformationObject(
      final String commit, 
      final String srcPath,
      final String dstPath,
      final CompilationUnit rule, 
      final ProjectInfo pi,
      final CompilationUnit dstUnit, 
      final ASTNode srcNode, 
      final ASTNode dstNode,
      Tuple<MethodDeclaration, MethodDeclaration> ba, 
      final List<Replacement<ASTNode>> src,
      final List<Replacement<ASTNode>> dst, 
      final DiffCalculator diff) {
    TransformationConfigObject config = new TransformationConfigObject();
    config.setCommit(commit);
    config.setSrcPath(srcPath);
    config.setDstPath(dstPath);
    config.setNodeSrc(srcNode);
    config.setNodeDst(dstNode);
    config.setSrcList(src);
    config.setDstList(dst);
    config.setRefasterRule(rule);
    config.setBa(ba);
    config.setDiff(diff);
    config.setDstCu(dstUnit);
    config.setPi(pi);
    return config;
  }

  /**
   * Checkout if folder where project is located contain a different commit.
   * @param srcEdit source edit
   */
  private static ProjectInfo checkoutIfDiffer(final Edit srcEdit) 
      throws IOException, GitAPIException {
    final Edit dstEdit = srcEdit.getDst();
    final ProjectInfo pi = ProjectAnalyzer.project(srcEdit);
    final String commit = dstEdit.getCommit();
    CommitUtils.checkoutIfDiffer(commit, pi);
    return pi;
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
    final KindNodeMatcher evaluator = new KindNodeMatcher(ASTNode.METHOD_DECLARATION);
    final MatchCalculator<ASTNode> mcal = new NodeMatchCalculator(evaluator);
    final List<ASTNode> nodes = mcal.getNodes(refasterRule);
    final MethodDeclaration before = (MethodDeclaration) nodes.get(0);
    final MethodDeclaration after = (MethodDeclaration) nodes.get(1);
    return new Tuple<>(before, after);
  }
}
