package br.ufcg.spg.refaster;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.diff.DiffCalculator;
import br.ufcg.spg.diff.DiffPath;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.git.CommitUtils;
import br.ufcg.spg.git.GitUtils;
import br.ufcg.spg.main.MainArguments;
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
import br.ufcg.spg.type.TypeUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
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
   * @param srcCluster
   *          cluster from the before version
   * @param rule
   *    *     RefasterRule
   * @param srcEdit
   *          Edit from de before version
   * @return before and after method for Refaster rule
   */
  private static Tuple<MethodDeclaration, MethodDeclaration> beforeAfter(
      final Cluster srcCluster, final CompilationUnit rule, Edit srcEdit)
      throws BadLocationException, IOException, GitAPIException {
    final Edit dstEdit = srcEdit.getDst();
    Tuple<CompilationUnit, CompilationUnit> units = getCompilationUnits(srcEdit, dstEdit);
    CompilationUnit srcUnit = units.getItem1();
    CompilationUnit dstUnit = units.getItem2();
    final ASTNode srcNode = getNode(srcEdit, srcUnit);
    final ASTNode dstNode = getNode(dstEdit, dstUnit);
    final String srcAu = srcCluster.getAu();
    final Cluster dstCluster = srcCluster.getDst();
    final String dstAu = dstCluster.getAu();    
    final List<Replacement<ASTNode>> src = ReplacementUtils.replacements(srcEdit, srcAu, srcUnit);
    final List<Replacement<ASTNode>> dst = ReplacementUtils.replacements(dstEdit, dstAu, dstUnit);
    // Return statement
    Tuple<MethodDeclaration, MethodDeclaration> ba = getBeforeAfterMethod(rule);
    ba = ReturnTypeTranslator.config(srcNode, dstNode, rule, ba);
    // Replace method body
    final DiffCalculator diff = new DiffPath("temp1.java", "temp2.java");
    diff.diff();
    String commit = dstEdit.getCommit();
    String srcPath = "temp1.java";
    String dstPath = "temp2.java";
    TransformationConfigObject config = configTransformationObject(
        commit, srcPath, dstPath, rule, dstEdit.getProject(),
        dstUnit, srcNode, dstNode, ba, src, dst, diff);
    ba = ReturnStmTranslator.config(config);
    return ba;
  }

  private static Tuple<CompilationUnit, CompilationUnit> getCompilationUnits(Edit srcEdit, Edit dstEdit) throws  IOException {
    //final ProjectInfo pi = checkoutIfDiffer(srcEdit);
    MainArguments main = MainArguments.getInstance();
    Repository repository = GitUtils.startRepo(main.getProjectFolder() + "/" + dstEdit.getProject());
    RevCommit revCommit = GitUtils.extractCommit(repository, dstEdit.getCommit());
    System.out.println(revCommit.getId().getName());
    RevCommit befCommit = new GitUtils().getPrevHash(repository, revCommit);
    String before = GitUtils.getEditedFile(repository, befCommit.getTree(), srcEdit.getPath());
    String after = GitUtils.getEditedFile(repository, revCommit.getTree(), dstEdit.getPath());
    FileUtils.writeStringToFile(new File("temp1.java"), before);
    FileUtils.writeStringToFile(new File("temp2.java"), after);
    CompilationUnit srcUnit = JParser.parse("temp1.java", before);
    CompilationUnit dstUnit = JParser.parse("temp2.java", after);
    return new Tuple<>(srcUnit, dstUnit);
  }

  private static ASTNode getNode(Edit edit, final CompilationUnit unit) {
    IMatcher<ASTNode> srcMatch = new PositionNodeMatcher(edit.getStartPos(), 
        edit.getEndPos());
    MatchCalculator<ASTNode> mcalc = new NodeMatchCalculator(srcMatch);
    final ASTNode srcNode = mcalc.getNode(unit);
    return srcNode;
  }

  /**
   * Setup transformation configuration object.
   */
  private static TransformationConfigObject configTransformationObject(
      final String commit, 
      final String srcPath,
      final String dstPath,
      final CompilationUnit rule, 
      final String pi,
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
