package br.ufcg.spg.editpair;

import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.antiunification.AntiUnifierUtils;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.comparer.ActionComparer;
import br.ufcg.spg.comparer.TreeComparer;
import br.ufcg.spg.component.ConnectedComponentManager;
import br.ufcg.spg.component.FullConnectedGumTree;
import br.ufcg.spg.dcap.DcapCalculator;
import br.ufcg.spg.diff.DiffCalculator;
import br.ufcg.spg.diff.DiffPath;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.equation.EquationUtils;
import br.ufcg.spg.expression.ExpressionManager;
import br.ufcg.spg.git.CommitUtils;
import br.ufcg.spg.git.GitUtils;
import br.ufcg.spg.imports.Import;
import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.PositionNodeMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.NodeMatchCalculator;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.path.PathUtils;
import br.ufcg.spg.project.ProjectAnalyzer;
import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.source.SourceUtils;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.util.PrintUtils;
import br.ufcg.spg.validator.node.NodeValidator;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;

public class EditPairCalculator {
  
  private EditPairCalculator() {
  }
  
  /**
   * Builds before and after list.
   * @param project project
   * @param files files
   * @param dstCommit target commit
   */
  public static List<Edit> computeEditPairs(final String project, List<String> files, 
      final String dstCommit) 
      throws IOException, GitAPIException {
    Run.initGenerators();
    // files to be analyzed
    final String projectFolderDst = "../Projects/" + project + "/";
    if (files == null) {
      final GitUtils analyzer = new GitUtils();
      files = analyzer.modifiedFiles(projectFolderDst, dstCommit);
    }
    if (files.isEmpty()) {
      return new ArrayList<>();
    }
    final String projectFolderSrc = "../Projects/" + project + "_old/";
    final List<String> srcFilePaths = new ArrayList<>();
    final List<String> dstFilePaths = new ArrayList<>();
    for (final String fileName : files) {
      final String srcFilePath = buildFilePath(projectFolderSrc, fileName);
      final String dstFilePath = buildFilePath(projectFolderDst, fileName);  
      srcFilePaths.add(srcFilePath);
      dstFilePaths.add(dstFilePath);
    }
    final String srcFileName = srcFilePaths.get(0);
    final String dstFileName = dstFilePaths.get(0);
    final ProjectInfo pi = ProjectAnalyzer.project(project, srcFileName, dstFileName);
    CommitUtils.checkoutIfDiffer(dstCommit, pi);
    return EditPairCalculator.extractEditPairs(srcFilePaths, 
        dstFilePaths, pi, dstCommit, project);
  }
  
  public static String buildFilePath(final String folderPath, final String filePath) {
    return folderPath + filePath;
  }

  /**
   * Extracts before and after version of the source code.
   * 
   * @param srcFilePaths
   *          list of modified files for the previous version
   * @param dstFilePaths
   *          the path for the list of files for the after version
   * @param pi
   *          information about the project
   */
  public static List<Edit> extractEditPairs(final List<String> srcFilePaths, 
      final List<String> dstFilePaths, final ProjectInfo pi,
      final String cmt, final String pj) 
          throws IOException {
    final List<Edit> srcEdits = new ArrayList<>();
    for (int i = 0; i < srcFilePaths.size(); i++) {
      final String srcPath = srcFilePaths.get(i);
      final String dstPath = dstFilePaths.get(i);
      final DiffCalculator diff = new DiffPath(srcPath, dstPath);
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
          final String pretty = first.getNode().toPrettyString(diff.getSrc());
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
      CompilationUnit unitSrc;
      CompilationUnit unitDst;
      try {
        // parse trees
        unitSrc = JParser.parse(srcPath, pi.getSrcVersion());
        unitDst = JParser.parse(dstPath, pi.getDstVersion());
      } catch (final OutOfMemoryError e) {
        e.printStackTrace();
        System.out.println(e);
        continue;
      }
      Collections.sort(roots, itreeComparer);
      System.out.println("FILE: " + srcPath);
      for (final ITree root : roots) {
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
        final ITree ctxSrc = unchagedContext(srcPath, diff.getSrc(), diff.getDst(), 
            srcNode, dstNode,
            cmt, mappings);
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
        /*System.out.println("Before creating upper edit!");
        final ASTNode srcUpper = ASTNodeUtils.getTopNode(srcAstNode);
        final ASTNode dstUpper = ASTNodeUtils.getTopNode(dstAstNode);
        if (srcUpper == null || dstUpper == null) {
          continue;
        }
        final Edit srcUpperEdit = createEdit(cmt, srcUpper, pj + "_old", srcPath, unitSrc);
        final String srcUpperEq = EquationUtils.convertToAuEq(srcUpper);
        final String dstUpperEq = EquationUtils.convertToAuEq(dstUpper);
        final Edit dstUpperEdit = createEdit(cmt, dstUpper, pj, dstPath, unitDst);
        System.out.println("After creating upper edit!");*/
        final Edit dstCtx = createEdit(cmt, fixedDst, pj, dstPath, unitDst);
        final Edit srcCtx = createEdit(cmt, fixedSrc, pj + "_old", srcPath, unitSrc);
        final Edit dstEdit = createEdit(cmt, dstAstNode, pj, dstPath, unitDst);
        final Edit srcEdit = createEdit(cmt, srcAstNode, pj + "_old", srcPath, unitSrc);
        //specific configuration to dst context
        srcCtx.setDst(dstCtx);
        //specific configuration to dst
        dstEdit.setContext(dstCtx);
        dstEdit.setTemplate(dstEq);
        /*dstEdit.setUpper(dstUpperEdit);
        dstUpperEdit.setTemplate(dstUpperEq);
        srcUpperEdit.setTemplate(srcUpperEq);
        configSrcEdit(cmt, srcEdit, dstEdit, srcCtx, srcUpperEdit, 
        srcEq, imports, srcAu, dstAu, pi);*/
        configSrcEdit(cmt, srcEdit, dstEdit, srcCtx, null, srcEq, imports, srcAu, dstAu, pi);
        srcEdits.add(srcEdit);
        showEditPair(srcPath, dstPath, srcNode, dstNode, fixedSrc, fixedDst);
      }
    }
    return srcEdits;
  }
  
  /**
   * Configure src edit.
   * @param srcEdit src edit
   * @param dstEdit dst edit
   * @param srcCtx src context
   * @param srcEq src template
   * @param imports import statements
   * @param srcAu src anti-unification
   * @param dstAu dst anti-unification
   */
  private static Edit configSrcEdit(final String cmt, final Edit srcEdit, 
      final Edit dstEdit, final Edit srcCtx,
      final Edit srcUpper, String srcEq, List<Import> imports, 
      AntiUnifier srcAu, AntiUnifier dstAu, final ProjectInfo pi) {
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
    final PersonIdent pident = gutils.getPersonIdent(pi.getDstVersion().getProject(), cmt);
    srcEdit.setDeveloper(pident.getName());
    srcEdit.setEmail(pident.getEmailAddress());
    srcEdit.setDate(pident.getWhen());
    return srcEdit;
  }
  
  private static Edit createEdit(String cmt, ASTNode node, String pj, 
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
   * Configures dcap for edit.
   * @param edit edit
   * @param antiUnifier anti-unification
   */
  private static void configDcap(final Edit edit, final AntiUnifier antiUnifier) {
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
  
  /**
   * Gets anti-unification.
   * @param astNode AST node
   * @param fixedNode fixed node
   * @return anti unification
   */
  private static AntiUnifier antiUnification(final ASTNode astNode, final ASTNode fixedNode) 
      throws IOException {
    return AntiUnifierUtils.template(astNode, astNode, fixedNode, fixedNode);
  }

  private static void showEditPair(final String src, final String dst, 
      final ITree srcNode, final ITree dstNode, 
      final ASTNode fixedSrc, final ASTNode fixedDst) throws IOException {
    // Log data
    final String str1 = new String(Files.readAllBytes(Paths.get(src)));
    System.out.print("(" + srcNode.getPos() + ", " + srcNode.getEndPos() + ") "
        + str1.substring(srcNode.getPos(), srcNode.getEndPos()));
    final String qualifiedNameSrc = ExpressionManager.qualifiedName(fixedSrc);
    if (qualifiedNameSrc != null) {
      System.out.print(": " + qualifiedNameSrc);
    }
    System.out.print(" --> ");
    final String str2 = new String(Files.readAllBytes(Paths.get(dst)));
    System.out.print("(" + dstNode.getPos() + ", " + dstNode.getEndPos() + ") "
        + str2.substring(dstNode.getPos(), dstNode.getEndPos()));
    final String qualifiedNameDst = ExpressionManager.qualifiedName(fixedDst);
    if (qualifiedNameDst != null) {
      System.out.print(": " + qualifiedNameSrc);
    }
    System.out.println();
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
  public static ITree unchagedContext(final String srcPath, final TreeContext src, 
      final TreeContext dst, final ITree srcNode, final ITree dstNode,
      final String commit, final MappingStore mapping) {
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
}
