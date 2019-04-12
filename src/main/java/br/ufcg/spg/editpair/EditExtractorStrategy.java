package br.ufcg.spg.editpair;

import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.antiunification.AntiUnifierUtils;
import br.ufcg.spg.bean.EditFile;
import br.ufcg.spg.dcap.DcapCalculator;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.expression.ExpressionManager;
import br.ufcg.spg.git.GitUtils;
import br.ufcg.spg.imports.Import;
import br.ufcg.spg.main.MainArguments;
import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.PositionNodeMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.NodeMatchCalculator;
import br.ufcg.spg.path.PathUtils;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.util.PrintUtils;

import com.github.gumtreediff.client.Run;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

public abstract class EditExtractorStrategy {
  
  private static final Logger logger = LogManager.getLogger(EditExtractorStrategy.class.getName());
  
  public abstract List<Edit> extractEditPairs(final List<EditFile> files, String project,
      final RevCommit cmt, final String pj) 
          throws IOException;
  
  /**
   * Builds before and after list.
   * @param project project
   * @param files files
   * @param dstCommit target commit
   */
  public static List<Edit> computeEditPairs(final String project, List<EditFile> files, 
      final RevCommit dstCommit) 
      throws IOException {
    MainArguments main = MainArguments.getInstance();
    Run.initGenerators();
    // files to be analyzed
    final String projectFolderDst = main.getProjectFolder() + '/' + project + "/";
    if (files == null) {
      final GitUtils analyzer = new GitUtils();
      files = analyzer.modifiedFiles(projectFolderDst, dstCommit);
    }
    if (files.isEmpty()) {
      return new ArrayList<>();
    }
    EditExtractorStrategy diffStrategy = new LineDiffStrategy();
    return diffStrategy.extractEditPairs(files, project, dstCommit, project);
  }

  public static String buildFilePath(final String folderPath, final String filePath) {
    return folderPath + filePath;
  }

  protected static Edit createEdit(String cmt, ASTNode node, String pj, 
      String dstPath, CompilationUnit unit) {
    IMatcher<ASTNode> matcher = new PositionNodeMatcher(node);
    final MatchCalculator<ASTNode> calc = new NodeMatchCalculator(matcher);
    final ASTNode astNode = calc.getNode(unit);
    final int startPos = astNode.getStartPosition();
    final int endPos = startPos + astNode.getLength();
    final int index = calc.getIndex(unit);
    final String dstCtxPath = PathUtils.computePathRoot(astNode);
    final String text = node.toString();
    return new Edit(cmt, startPos, endPos, index, pj, 
        dstPath, dstCtxPath, null, null, null, text);
  }
  
  /**
   * Gets anti-unification.
   * @param astNode AST node
   * @param fixedNode fixed node
   * @return anti unification
   */
  protected static AntiUnifier antiUnification(final ASTNode astNode, final ASTNode fixedNode) 
      throws IOException {
    return AntiUnifierUtils.template(astNode, astNode, fixedNode, fixedNode);
  }
  
  protected static void showEditPair(final ASTNode srcNode, final ASTNode dstNode, 
      final ASTNode fixedSrc, final ASTNode fixedDst) {
    // Log data
    final int startSrc = srcNode.getStartPosition();
    final int endSrc = startSrc + srcNode.getLength();
    StringBuilder sb = new StringBuilder();
    sb.append("(" + startSrc + ", " + endSrc + ") "
        + srcNode);
    final String qualifiedNameSrc = ExpressionManager.qualifiedName(fixedSrc);
    if (qualifiedNameSrc != null) {
      sb.append(": " + qualifiedNameSrc);
    }
    sb.append(" --> ");
    final int startDst = dstNode.getStartPosition();
    final int endDst = startDst + dstNode.getLength();
    sb.append("(" + startDst + ", " + endDst + ") "
        + dstNode);
    final String qualifiedNameDst = ExpressionManager.qualifiedName(fixedDst);
    if (qualifiedNameDst != null) {
      sb.append(": " + qualifiedNameSrc);
    }
    logger.trace(sb.toString());
  }

  
  /**
   * Configure src edit.
   */
  protected static Edit configSrcEdit(final RevCommit cmt, final Edit srcEdit, 
      final Edit dstEdit, final Edit srcCtx,
      final Edit srcUpper, String srcEq, List<Import> imports, 
      AntiUnifier srcAu, AntiUnifier dstAu, final String project) {
    //specific configuration to src
    srcEdit.setDst(dstEdit);
    srcEdit.setContext(srcCtx);
    srcEdit.setUpper(srcUpper);
    srcEdit.setTemplate(srcEq);
    //other configurations
    dstEdit.setImports(imports);
    configDcap(srcEdit, srcAu);
    configDcap(dstEdit, dstAu);
    final GitUtils gutils = new GitUtils();
    final PersonIdent pident = gutils.getPersonIdent(cmt, project, cmt.getId().getName());
    srcEdit.setDeveloper(pident.getName());
    srcEdit.setEmail(pident.getEmailAddress());
    srcEdit.setDate(pident.getWhen());
    return srcEdit;
  }
  

  /**
   * Configures dcap for edit.
   * @param edit edit
   * @param antiUnifier anti-unification
   */
  protected static void configDcap(final Edit edit, final AntiUnifier antiUnifier) {
    final RevisarTree<String> srcTreeD3 = DcapCalculator.dcap(antiUnifier, 3);
    final String srcDcapD3 = PrintUtils.prettyPrint(srcTreeD3);
    final RevisarTree<String> srcTreeD2 = DcapCalculator.dcap(antiUnifier, 2);
    final String srcDcapD2 = PrintUtils.prettyPrint(srcTreeD2);
    final RevisarTree<String> srcTreeD1 = DcapCalculator.dcap(antiUnifier, 1);
    final String srcDcapD1 = PrintUtils.prettyPrint(srcTreeD1);
    edit.setDcap3(srcDcapD3);
    edit.setDcap2(srcDcapD2);
    edit.setDcap1(srcDcapD1);
  }
}
