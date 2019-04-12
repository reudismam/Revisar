package br.ufcg.spg.editpair;

import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.bean.EditFile;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.comparer.ActionComparer;
import br.ufcg.spg.comparer.TreeComparer;
import br.ufcg.spg.component.ConnectedComponentManager;
import br.ufcg.spg.component.FullConnectedGumTree;
import br.ufcg.spg.diff.DiffCalculator;
import br.ufcg.spg.diff.DiffPath;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.imports.Import;
import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.PositionNodeMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.NodeMatchCalculator;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.source.SourceUtils;
import br.ufcg.spg.validator.node.NodeValidator;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jgit.revwalk.RevCommit;

public class GumTreeDiffStrategy extends EditExtractorStrategy {

  private static final Logger logger = LogManager.getLogger(LineDiffStrategy.class.getName());

  @Override
  public List<Edit> extractEditPairs(List<EditFile> files, String project, RevCommit cmt, String pj)
      throws IOException {
    final List<Edit> srcEdits = new ArrayList<>();
    for (int i = 0; i < files.size(); i++) {
      EditFile file = files.get(i);
      String srcSource = file.getBeforeAfter().getItem1();
      String dstSource = file.getBeforeAfter().getItem2();
      FileUtils.writeStringToFile(new File("temp1.java"), srcSource);
      FileUtils.writeStringToFile(new File("temp2.java"), dstSource);
      final DiffCalculator diff = new DiffPath("temp1.java", "temp2.java");
      List<Action> actions = null;
      try {
        actions = diff.diff();
      } catch (final Exception e) {
        continue;
      }
      if (actions.isEmpty()) {
        // if no action is found process the next file.
        continue;
      }      
      // parse trees
      CompilationUnit  unitSrc = JParser.parse("temp1.java", srcSource);
      CompilationUnit  unitDst = JParser.parse("temp2.java", dstSource);
      String srcPath = file.getSrcPath();
      String dstPath = file.getDstPath();
      logger.trace("FILE: " + srcPath);
      Tuple<List<ITree>, List<Import>> tu = processActions(actions, diff.getSrc(), dstPath);
      List<Edit> edits = computeEdits(project, cmt, pj, file, diff, unitSrc, unitDst, tu);
      srcEdits.addAll(edits);
    }
    return srcEdits;
  }

  private List<Edit> computeEdits(String project, RevCommit cmt, String pj, EditFile file,
      final DiffCalculator diff, CompilationUnit unitSrc, 
      CompilationUnit unitDst, Tuple<List<ITree>, List<Import>> tu)
      throws IOException {
    final List<Edit> srcEdits = new ArrayList<>();
    for (final ITree root : tu.getItem1()) {
      final MappingStore mappings = diff.getMatcher().getMappings();
      final Tuple<ITree, ITree> beforeafter = beforeAfter(mappings, root,
          diff.getSrc());
      if (beforeafter == null) {
        continue;
      }
      final ITree srcNode = beforeafter.getItem1();
      final ITree dstNode = beforeafter.getItem2();
      // get ASTNode in compilation unit
      IMatcher<ASTNode> srcMatcher = new PositionNodeMatcher(srcNode);
      final MatchCalculator<ASTNode> srcMa = new NodeMatchCalculator(srcMatcher);
      final ASTNode srcAstNode = srcMa.getNode(unitSrc);
      final ITree ctxSrc = unchagedContext(diff.getSrc(), diff.getDst(), 
          srcNode, dstNode, mappings);
      final boolean isSingleLineSrc = SourceUtils.isSingleLine(unitSrc, ctxSrc.getPos(), 
          ctxSrc.getEndPos());
      // get ASTNode for node with unchanged context in compilation unit
      final IMatcher<ASTNode> fsrcMatcher = new PositionNodeMatcher(ctxSrc);
      final MatchCalculator<ASTNode> fsrcMa = new NodeMatchCalculator(fsrcMatcher);
      final ASTNode fixedSrc = fsrcMa.getNode(unitSrc);
      // get ASTNode for fixedDst
      final IMatcher<ASTNode> dstMatcher = new PositionNodeMatcher(dstNode);
      final MatchCalculator<ASTNode> dstMa = new NodeMatchCalculator(dstMatcher);
      final ASTNode dstAstNode = dstMa.getNode(unitDst);
      final ITree ctxDst = mappings.getDst(ctxSrc);
      final IMatcher<ASTNode> fdstMatcher = new PositionNodeMatcher(ctxDst);
      final MatchCalculator<ASTNode> fdstMa = new NodeMatchCalculator(fdstMatcher);
      final ASTNode fixedDst = fdstMa.getNode(unitDst);
      final boolean isSingleLineDst = SourceUtils.isSingleLine(unitDst, ctxDst.getPos(), 
          ctxDst.getEndPos());
      if (!isSingleLineSrc || !isSingleLineDst) {
        continue;
      }
      if (srcAstNode == null || fixedSrc == null || dstAstNode == null || fixedDst == null) {
        continue;
      }      
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
      //specific configuration to dst context
      srcCtx.setDst(dstCtx);
      configSrcEdit(cmt, srcEdit, dstEdit, srcCtx, null, srcEq, tu.getItem2(), srcAu, dstAu, project);
      srcEdits.add(srcEdit);
      showEditPair(srcAstNode, dstAstNode, fixedSrc, fixedDst);
    }
    return srcEdits;
  }
  
  public Tuple<List<ITree>, List<Import>> processActions(List<Action> actions, TreeContext src, 
      String dstPath) throws IOException { 
    final ConnectedComponentManager<Action> con = 
        new ConnectedComponentManager<>();
    final List<List<Action>> actionList = 
        con.connectedComponents(actions, new FullConnectedGumTree(actions));
    // Comparer
    final Comparator<Action> actionComparer = new ActionComparer();
    final Comparator<ITree> itreeComparer = new TreeComparer();
    final List<ITree> roots = new ArrayList<>();
    final List<Import> imports = new ArrayList<>();
    for (final List<Action> list : actionList) {
      Collections.sort(list, actionComparer);
      final Action first = list.get(0);
      if (first instanceof Insert || first instanceof Move || first instanceof Update) {
        final String pretty = first.getNode().toPrettyString(src);
        if (pretty.equals("ImportDeclaration")) {
          final int start = first.getNode().getPos();
          final int end = first.getNode().getEndPos();
          final String text = FileUtils.readFileToString(new File(dstPath)).substring(start, end);
          final Import importStm = new Import(start, end, text);
          imports.add(importStm);
          roots.add(first.getNode());
        } else {
          roots.add(first.getNode().getParent());
        }
      } else {
        roots.add(first.getNode());
      }
    }
    Collections.sort(roots, itreeComparer);
    return new Tuple<>(roots, imports);
  }
   
  /**
   * Gets the maximum unchanged context.
   * 
   * @param src
   *          Source context
   * @param dst
   *          Destination context
   * @param srcNode
   *          Source
   * @param dstNode
   *          Destination
   * @return maximum unchanged context
   */
  public static ITree unchagedContext(final TreeContext src, 
      final TreeContext dst, final ITree srcNode, final ITree dstNode, final MappingStore mapping) {
    ITree tempSrc = srcNode;
    ITree tempDst = dstNode;
    final List<ITree> pathSrc = new ArrayList<>();
    final List<ITree> pathDst = new ArrayList<>();
    while (!(tempSrc.toPrettyString(src).equals("CompilationUnit"))) {
      pathSrc.add(tempSrc);
      tempSrc = tempSrc.getParent();
    }
    pathSrc.add(tempSrc);

    while (!(tempDst.toPrettyString(dst).equals("CompilationUnit"))) {
      pathDst.add(tempDst);
      tempDst = tempDst.getParent();
    }
    pathDst.add(tempDst);
    Collections.reverse(pathSrc);
    Collections.reverse(pathDst);
    for (int i = 0; i < pathSrc.size(); i++) {
      if (!mapping.has(pathSrc.get(i), pathDst.get(i))) {
        return pathSrc.get(i - 1);
      }
    }
    return srcNode;
  }
  
  /**
   * Computes before and after nodes.
   * @param mapping
   *          mapping between before and after tree.
   * @param root
   *          root node
   * @param src
   *          tree context
   * @return before and after version of the file.
   */
  public static Tuple<ITree, ITree> beforeAfter(
      final MappingStore mapping, final ITree root, final TreeContext src) {
    ITree srcNode = null;
    ITree dstNode = null;
    if (mapping.hasDst(root)) {
      srcNode = mapping.getSrc(root);
      dstNode = root;
    } else if (mapping.hasSrc(root)) {
      srcNode = root;
      dstNode = mapping.getDst(root);
    } else {
      final ITree parent = root.getParent();
      final String pretty = parent.toPrettyString(src);
      if (pretty.equals("CompilationUnit")) {
        return null;
      }
      if (mapping.hasDst(parent)) {
        srcNode = mapping.getSrc(parent);
        dstNode = root;
      } else if (mapping.hasSrc(parent)) {
        srcNode = parent;
        dstNode = mapping.getDst(parent);
      } else {
        final String source = root.toPrettyString(src);
        System.out.println(source + " has been inserted or deleted from the source code.");
        return null;
      }
    }
    return new Tuple<>(srcNode, dstNode);
  }

}
