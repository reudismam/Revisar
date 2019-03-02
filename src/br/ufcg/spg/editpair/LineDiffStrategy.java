package br.ufcg.spg.editpair;

import br.ufcg.spg.analyzer.test.TestSuite;
import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.bean.EditFile;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.validator.node.NodeValidator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * Calculates edit operations based on Git Line diff.
 */
public class LineDiffStrategy extends EditExtractorStrategy {
  
  private static final Logger logger = LogManager.getLogger(LineDiffStrategy.class.getName());

  @Override
  public List<Edit> extractEditPairs(List<EditFile> files, String project, RevCommit cmt, String pj)
      throws IOException {
    final List<Edit> srcEdits = new ArrayList<>();
    for (EditFile file : files) {
      String srcSource = file.getBeforeAfter().getItem1();
      String dstSource = file.getBeforeAfter().getItem2();
      FileUtils.writeStringToFile(new File("temp1.java"), srcSource);
      FileUtils.writeStringToFile(new File("temp2.java"), dstSource);
      CompilationUnit unitSrc;
      CompilationUnit unitDst;
      try {
        // parse trees
        unitSrc = JParser.parse("temp1.java", srcSource);
        unitDst = JParser.parse("temp2.java", dstSource);
      } catch (final OutOfMemoryError e) {
        logger.trace("Out of Memory.");
        continue;
      }    
      for (Tuple<ASTNode, ASTNode> tuple : file.getEdits()) {
        final ASTNode srcAstNode = tuple.getItem1();
        final ASTNode dstAstNode = tuple.getItem2();
        final ASTNode fixedSrc = srcAstNode;
        final ASTNode fixedDst = dstAstNode;
        final AntiUnifier srcAu = antiUnification(srcAstNode, fixedSrc);
        final AntiUnifier dstAu = antiUnification(dstAstNode, fixedDst);
        final String srcEq = EquationUtils.convertToEquation(srcAu);
        final String dstEq = EquationUtils.convertToEquation(dstAu);
        if (!NodeValidator.isValidNode(srcEq) || !NodeValidator.isValidNode(dstEq)) {
          continue;
        }
        String cmtStr = cmt.getId().getName();
        final Edit dstCtx = createEdit(cmtStr, fixedDst, pj, file.getDstPath(), unitDst);
        final Edit srcCtx = createEdit(cmtStr, fixedSrc, pj + "_old", file.getSrcPath(), unitSrc);
        final Edit dstEdit = createEdit(cmtStr, dstAstNode, pj, file.getDstPath(), unitDst);
        final Edit srcEdit = createEdit(cmtStr, srcAstNode, pj + "_old", file.getSrcPath(), unitSrc);
        if (srcEdit.getText().equals(dstEdit.getText())) {
          continue;
        }
        // specific configuration to dst context
        srcCtx.setDst(dstCtx);
        // specific configuration to dst
        dstEdit.setContext(dstCtx);
        dstEdit.setTemplate(dstEq);
        configSrcEdit(cmt, srcEdit, dstEdit, srcCtx, null, srcEq, new ArrayList<>(), srcAu, dstAu, project);
        srcEdits.add(srcEdit);
      }
    }
    return srcEdits;
  }
}
