package br.ufcg.spg.dependence;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.compile.CompilerUtils;
import br.ufcg.spg.constraint.ConstraintUtils;
import br.ufcg.spg.database.DependenceDao;
import br.ufcg.spg.database.EditDao;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.edit.EditUtils;
import br.ufcg.spg.exp.ExpUtils;
import br.ufcg.spg.git.CommitUtils;
import br.ufcg.spg.imports.Import;
import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.KindNodeMatcher;
import br.ufcg.spg.matcher.PositionNodeMatcher;
import br.ufcg.spg.matcher.PositionRevisarTreeMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.NodeMatchCalculator;
import br.ufcg.spg.matcher.calculator.RevisarTreeMatchCalculator;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.project.ProjectAnalyzer;
import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.tree.RevisarTree;
import br.ufcg.spg.tree.RevisarTreeUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;

public class DependenceUtils {
  
  /**
   * Compiles the code before and after the change.
   */
  public static void dependences()
      throws IOException, ExecutionException, GitAPIException {
    final List<Tuple<String, String>> projs = ExpUtils.getProjects();
    final Edit lastEdit = DependenceDao.getInstance().lastDependence();
    final List<Tuple<String, String>> projects = projects(projs, lastEdit);
    for (final Tuple<String, String> project : projects) {
      final String pname = project.getItem1();
      final List<String> commits = EditDao.getInstance().getAllCommits(pname);
      int index = 0;
      if (project.getItem2() != null) {
        index = commits.indexOf(project.getItem1()) + 1;
      }
      for (int c = index; c < commits.size(); c++) {
        System.out.println(((double)c) / commits.size() + " % complete");
        final String commit = commits.get(c);
        final Map<Edit, List<Edit>> graph = computeGraph(commit);
        save(graph);
      }
    }
  }
  
  /**
   * Identifies the dependences for specified edit.
   * @param srcEdit source code edit.
   * @return dependences
   */
  public static List<ASTNode> dependences(final Edit srcEdit, List<ASTNode> errorsSrc) 
      throws IOException, ExecutionException {
    final Edit dstEdit = srcEdit.getDst();
    final List<Import> imports = dstEdit.getImports();
    //dst text and location where srcEdit goes to in the dst tree.
    final Tuple<String, Tuple<Integer, Integer>> edit = edit(srcEdit, srcEdit.getDst(), imports);
    final String after = edit.getItem1();
    final ProjectInfo pi = ProjectAnalyzer.project(srcEdit);
    //before and after node.
    final String srcFile = srcEdit.getPath();
    final Tuple<CompilationUnit, CompilationUnit> baEdit = 
        EditUtils.beforeAfter(srcFile, after);
    final CompilationUnit srcRoot = baEdit.getItem1();
    final Tuple<Integer, Integer> locationDst = edit.getItem2();
    final CompilationUnit dstRoot = baEdit.getItem2();
    final IMatcher<ASTNode> positionMatcher = new PositionNodeMatcher(
        locationDst.getItem1(), locationDst.getItem2());
    final MatchCalculator<ASTNode> mcalc = new NodeMatchCalculator(positionMatcher);
    final ASTNode mappedDstNode = mcalc.getNode(dstRoot);
    if (mappedDstNode == null) {
      return new ArrayList<ASTNode>();
    }
    //identify broken constraints in target code.
    List<ASTNode> dstErrors = ConstraintUtils.constraints(dstRoot);
    dstErrors = removeIntersect(dstErrors, mappedDstNode);
    System.out.println(errorsSrc.size() + " : " + dstErrors.size());
    final List<ASTNode> mapped = mapped(dstErrors, srcRoot, dstRoot);
    final List<ASTNode> diffs = diffs(errorsSrc, mapped);
    return diffs;
  }

  private static CompilationUnit getCompilationUnit(final Edit srcEdit, final Edit dstEdit) 
      throws IOException, GitAPIException {
    //checkout the commit if current commit differs.
    final ProjectInfo pi = ProjectAnalyzer.project(srcEdit);
    CommitUtils.checkoutIfDiffer(dstEdit.getCommit(), pi);
    final String srcFile = srcEdit.getPath();
    final CompilationUnit compSrc = JParser.parse(srcFile, pi.getSrcVersion());
    return compSrc;
  }

  private static List<ASTNode> getErrorsSrc(final Edit srcEdit, final CompilationUnit srcRoot) {
    //identify broken constraints in source code..
    List<ASTNode> errorsSrc = ConstraintUtils.constraints(srcRoot);
    final IMatcher<ASTNode> srcMatcher = new PositionNodeMatcher(srcEdit.getStartPos(), 
        srcEdit.getEndPos());
    final MatchCalculator<ASTNode> mcal = new NodeMatchCalculator(srcMatcher);
    final ASTNode srcNode = mcal.getNode(srcRoot);
    errorsSrc = removeIntersect(errorsSrc, srcNode);
    return errorsSrc;
  }
  
  /**
   * Save dependencies.
   * @param graph graph of dependencies
   */
  private static void save(final Map<Edit, List<Edit>> graph) {
    for (final Edit key : graph.keySet()) {
      final Dependence dependence = new Dependence();
      dependence.setEdit(key);
      dependence.setNodes(new ArrayList<>());
      final List<Edit> value = graph.get(key);
      for (final Edit edit : value) {
        if (!dependence.getNodes().contains(edit)) {
          dependence.addNode(edit);
        }
      }
      final DependenceDao dao = DependenceDao.getInstance();
      dao.save(dependence);
    }  
  }

  /**
   * Computes graph.
   * @param commit commit to be analyzed
   */
  public static Map<Edit, List<Edit>> computeGraph(final String commit)
      throws IOException, ExecutionException, GitAPIException {
    final List<Edit> srcEdits = EditDao.getInstance().getSrcEdits(commit);
    return computeGraph(srcEdits);
  }
  
  /**
   * Computes graph.
   */
  public static Map<Edit, List<Edit>> computeGraph(final List<Edit> srcEdits)
      throws IOException, ExecutionException, GitAPIException {
    final Map<Edit, List<Edit>> graph = new Hashtable<>();
    final Map<String, List<ASTNode>> errorsSrc = new HashMap<>();
    final Map<String, List<Edit>> fileEdits = new HashMap<>();
    // fill each node with an empty list
    for (final Edit edit : srcEdits) {
      graph.put(edit, new ArrayList<>());
      if (!fileEdits.containsKey(edit.getPath())) {
        fileEdits.put(edit.getPath(), new ArrayList<>());
      }
      List<Edit> mp = fileEdits.get(edit.getPath());
      mp.add(edit);
      fileEdits.put(edit.getPath(), mp);
    }
    for (int i = 0; i < srcEdits.size(); i++) {
      final Edit srcEdit = srcEdits.get(i);
      List<ASTNode> errors;
      if (errorsSrc.containsKey(srcEdit.getPath())) {
        errors = errorsSrc.get(srcEdit.getPath());
      } else {
        CompilationUnit srcRoot = getCompilationUnit(srcEdit, srcEdit.getDst());
        errors = getErrorsSrc(srcEdit, srcRoot);
        errorsSrc.put(srcEdit.getPath(), errors);
      }
      if (errors.size() > 1000) {
        continue;
      }
      final List<ASTNode> related = dependences(srcEdit, errors);
      for (final ASTNode node : related) {
        final Edit original = original(fileEdits.get(srcEdit.getPath()), node);
        if (original != null) {
          final List<Edit> listNode = graph.get(srcEdit);
          if (!listNode.contains(original)) {
            graph.get(srcEdit).add(original);
            graph.get(original).add(srcEdit);
          }
        }
      }
    }
    save(graph);
    return graph;
  }

  /**
   * Gets remaining projects.
   * 
   * @param projects
   *          projects
   * @param edit
   *          last edit
   * @return remaining project
   */
  private static List<Tuple<String, String>> projects(final List<Tuple<String, String>> 
      projects, final Edit edit) {
    final List<Tuple<String, String>> remainProjs = new ArrayList<>();
    if (edit == null) {
      return projects;
    }
    final String pname = edit.getProject();
    final String lastCommit = edit.getCommit();
    boolean include = false;
    for (final Tuple<String, String> pj : projects) {
      if ((pj.getItem1() + "_old").equals(pname)) {
        include = true;
        pj.setItem2(lastCommit);
      }
      if (include) {
        remainProjs.add(pj);
      }
    }
    return remainProjs;
  }

  /**
   * Gets the original version of the node.
   * 
   * @param allNodes
   *          all nodes
   * @param node
   *          node to find original node
   * @return original node
   */
  private static Edit original(final List<Edit> allNodes, final ASTNode node) 
      throws ExecutionException {
    for (int i = 0; i < allNodes.size(); i++) {
      final Edit edit = allNodes.get(i);
      final ProjectInfo pi = ProjectAnalyzer.project(edit);
      final String commit = edit.getCommit();
      final CompilationUnit cu = CompilerUtils.getCunit(edit, commit, pi.getSrcVersion(), pi);
      final IMatcher<ASTNode> matcher = new PositionNodeMatcher(edit.getStartPos(), 
          edit.getEndPos());
      final MatchCalculator<ASTNode> mcal = new NodeMatchCalculator(matcher);
      final ASTNode foundNode = mcal.getNode(cu);
      if (foundNode != null && intersect(node, foundNode)) {
        return edit;
      }
    }
    return null;
  }

  /**
   * Determines whether two nodes intersect themselves.
   * @param nodei first node
   * @param nodej second node
   */
  private static boolean intersect(final ASTNode nodei, final ASTNode nodej) {
    final int starti = nodei.getStartPosition();
    final int endi = starti + nodei.getLength();
    final int startj = nodej.getStartPosition();
    final int endj = startj + nodej.getLength();
    final boolean iandj = starti <= startj && startj <= endi;
    final boolean jandi = startj <= starti && starti < endj;
    return iandj || jandi;
  }

  /**
   * Removes constraints of node that intersect with specified node.
   * @param errorsSrc nodes to be analyzed.
   * @param targetNode node.
   */
  private static List<ASTNode> removeIntersect(final List<ASTNode> errorsSrc, 
      final ASTNode targetNode) {
    final List<ASTNode> nodes = new ArrayList<ASTNode>();
    for (final ASTNode node : errorsSrc) {
      if (!intersect(targetNode, node)) {
        nodes.add(node);
      }
    }
    return nodes;
  }

  /**
   * From the destination node list, gets the nodes in the source code that
   * corresponds to destination nodes.
   * 
   * @param dstNodes
   *          nodes.
   * @param srcRoot
   *          root of the source code tree
   * @param dstRoot
   *          root of the destination source code
   */
  private static List<ASTNode> mapped(final List<ASTNode> dstNodes, 
      final ASTNode srcRoot, final ASTNode dstRoot) {
    final List<ASTNode> result = new ArrayList<>();
    RevisarTree<ASTNode> srcrtree = RevisarTreeUtils.convertToRevisarTree(srcRoot);
    RevisarTree<ASTNode> dstrtree = RevisarTreeUtils.convertToRevisarTree(dstRoot);
    for (final ASTNode dstNode : dstNodes) {
      IMatcher<RevisarTree<ASTNode>> match = new PositionRevisarTreeMatcher<>(dstNode);
      final MatchCalculator<RevisarTree<ASTNode>> mcalc = 
          new RevisarTreeMatchCalculator<>(match);
      final RevisarTree<ASTNode> rtree = mcalc.getNode(dstrtree);
      if (rtree == null) {
        System.out.println();
      }
      final ArrayList<Integer> path = RevisarTreeUtils.getPathToRoot(rtree);
      IMatcher<ASTNode> importMatch = new KindNodeMatcher(ASTNode.IMPORT_DECLARATION);
      MatchCalculator<ASTNode> importcalc = new NodeMatchCalculator(importMatch);
      List<ASTNode> srcImports = importcalc.getNodes(srcRoot);
      List<ASTNode> dstImports = importcalc.getNodes(dstRoot);
      //needed since dst imports may differ from previous version.
      if (!path.isEmpty()) {
        path.set(0, path.get(0) - (dstImports.size() - srcImports.size()));
      }
      final RevisarTree<ASTNode> srcNode = RevisarTreeUtils.getNodeFromPath(srcrtree, path);
      result.add(srcNode.getValue());
    }
    return result;
  }
  
  /**
   * gets the diff between before and after version.
   * @param errorsSrc source version
   * @param errorsDst destination  version
   */
  private static List<ASTNode> diffs(final List<ASTNode> errorsSrc, final List<ASTNode> errorsDst) {
    final HashSet<Tuple<Integer, Integer>> set = new HashSet<>();
    final List<ASTNode> result = new ArrayList<>();
    for (final ASTNode srcNode : errorsSrc) {
      Tuple<Integer, Integer> tu = new Tuple<>(srcNode.getStartPosition(), srcNode.getLength());
      set.add(tu);
    }   
    for (final ASTNode dstNode: errorsDst) {
      Tuple<Integer, Integer> tu = new Tuple<>(dstNode.getStartPosition(), dstNode.getLength());
      if (!set.contains(tu)) {
        result.add(dstNode);
      }
    }
    return result;
  }

  /**
   * Return the modified version of the node.
   * @param srcEdit
   *          before version of the node
   * @param dstEdit
   *          after version of the node
   * @param imports
   *          import statements
   * @return modified version and index of modified node
   */
  private static Tuple<String, Tuple<Integer, Integer>> edit(final Edit srcEdit,
      final Edit dstEdit, final List<Import> imports) 
          throws IOException, ExecutionException {
    final String srcFilePath = srcEdit.getPath();
    final String dstFilePath = dstEdit.getPath();
    final ProjectInfo pi =  ProjectAnalyzer.project(srcEdit);
    final String srcCommit = dstEdit.getCommit();
    final CompilationUnit srcCu = CompilerUtils.getCunit(srcEdit, 
        srcCommit, pi.getSrcVersion(), pi);
    final CompilationUnit dstCu = CompilerUtils.getCunit(dstEdit, 
        dstEdit.getCommit(), pi.getDstVersion(), pi);
    IMatcher<ASTNode> matcher = new PositionNodeMatcher(srcEdit.getStartPos(), 
        srcEdit.getEndPos());
    MatchCalculator<ASTNode> mcal = new NodeMatchCalculator(matcher);
    final ASTNode srcNode = mcal.getNode(srcCu);
    matcher = new PositionNodeMatcher(dstEdit.getStartPos(), dstEdit.getEndPos());
    mcal = new NodeMatchCalculator(matcher);
    final ASTNode dstNode = mcal.getNode(dstCu);
    String srcContent = "";
    String dstContent = "";
    try {
      srcContent = new String(Files.readAllBytes(Paths.get(srcFilePath)));
      dstContent = new String(Files.readAllBytes(Paths.get(dstFilePath)));
    } catch (final IOException e) {
      e.printStackTrace();
    }
    final String beforeEdit = srcContent.substring(0, srcNode.getStartPosition());
    final int dstStart = dstNode.getStartPosition();
    final int dstLength = dstNode.getStartPosition() + dstNode.getLength();
    final int srcLength = srcNode.getStartPosition() + srcNode.getLength();
    if (dstLength >= dstContent.length()) {
      System.out.println();
    }
    final String change = dstContent.substring(dstStart, dstLength);
    final String afterEdit = srcContent.substring(srcLength);
    int start = beforeEdit.length();
    String result = beforeEdit + change + afterEdit;
    for (final Import ttree : imports) {
      final int i = result.indexOf("import");
      final String b = result.substring(0, i);
      final String c = ttree.getText() + '\n';
      final String a = result.substring(i);
      result = b + c + a;
      start += c.length();
    }
    final Tuple<Integer, Integer> location = new Tuple<>(start, start + change.length());
    return new Tuple<>(result, location);
  }

  /**
   * Returns the list of errors.
   * 
   * @param srcFilePath
   *          - path
   * @param errors
   *          - errors
   * @return the list of errors
   */
  public List<br.ufcg.spg.bean.Error> getErrors(final String srcFilePath, 
      final List<br.ufcg.spg.bean.Error> errors) {
    final File file = new File(srcFilePath);
    String absolutePath = "";
    try {
      absolutePath = file.getCanonicalPath();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    final List<br.ufcg.spg.bean.Error> errorList = new ArrayList<>();
    for (final br.ufcg.spg.bean.Error er : errors) {
      if (er.getFile().equals(absolutePath)) {
        errorList.add(er);
      }
    }
    return errorList;
  }
}
