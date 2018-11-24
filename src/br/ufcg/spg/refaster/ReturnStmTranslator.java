package br.ufcg.spg.refaster;

import br.ufcg.spg.bean.Template;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.diff.DiffCalculator;
import br.ufcg.spg.diff.DiffTreeContext;
import br.ufcg.spg.edit.EditUtils;
import br.ufcg.spg.git.CommitUtils;
import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.PositionNodeMatcher;
import br.ufcg.spg.matcher.PositionTreeMatcher;
import br.ufcg.spg.matcher.ValueNodeMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.NodeMatchCalculator;
import br.ufcg.spg.matcher.calculator.TreeMatchCalculator;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.project.Version;
import br.ufcg.spg.refaster.config.ReturnStatementConfig;
import br.ufcg.spg.refaster.config.TransformationConfigObject;
import br.ufcg.spg.replacement.Replacement;
import br.ufcg.spg.replacement.ReplacementUtils;
import com.github.gumtreediff.matchers.Matcher;
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
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.text.edits.TextEdit;

public class ReturnStmTranslator {
  
  /**
   * Configures return statement.
   * @return before and after method with return type configured.
   */
  public static Tuple<MethodDeclaration, MethodDeclaration> config(
      final TransformationConfigObject config) throws BadLocationException, 
      IOException, NoFilepatternException, GitAPIException {
    final Tuple<List<ASTNode>, List<ASTNode>> bas = ReplacementUtils.mapping(config);
    final List<ASTNode> befores = bas.getItem1();
    final List<ASTNode> afters = bas.getItem2();
    ReturnStatementConfig bconfig = getReturnStatementConfig(
        config.getCommit(), config.getSrcPath(),
        config.getNodeSrc(), config.getSrcList(),
        config.getBa().getItem1(), config.getPi().getSrcVersion(), 
        config, befores);
    ReturnStatementConfig aconfig = getReturnStatementConfig(
        config.getCommit(), config.getDstPath(),
        config.getNodeDst(), config.getDstList(),
        config.getBa().getItem2(), config.getPi().getDstVersion(), 
        config, afters);
    MethodDeclaration before = addReturnStatement(bconfig);
    MethodDeclaration after = addReturnStatement(aconfig);
    return new Tuple<>(before, after);
  }

  /**
   * Create a return statement configuration.
   * @param target target node
   * @param targetList nodes in target.
   * @param m method in Refaster rule
   * @param version source or target version
   * @param config configuration
   * @param nodes list of nodes.
   */
  private static ReturnStatementConfig getReturnStatementConfig(
      final String commit, final String path,
      ASTNode target, List<Replacement<ASTNode>> targetList, 
      MethodDeclaration m, Version version, 
      TransformationConfigObject config, final List<ASTNode> nodes) {
    ReturnStatementConfig rconfig = new ReturnStatementConfig();
    rconfig.setCommit(commit);
    rconfig.setPath(path);
    rconfig.setTarget(target);
    rconfig.setNodes(nodes);
    rconfig.setTargetList(targetList);
    rconfig.setRefasterRule(config.getRefasterRule());
    rconfig.setMethod(m);
    rconfig.setVersion(version);
    rconfig.setPi(config.getPi());
    return rconfig;
  }
  
  /**
   * Add a return statement to a method body.
   * @param rconf return statement configuration.
   */
  private static MethodDeclaration addReturnStatement(ReturnStatementConfig rconf) 
          throws BadLocationException, IOException, NoFilepatternException, GitAPIException {
    final AST ast = rconf.getRefasterRule().getAST();
    final Template template = getTemplate(rconf);
    if (template == null) {
      return rconf.getMethod();
    }
    ReturnStatement rstm = (ReturnStatement) rconf.getMethod().getBody().statements().get(0);
    rstm = (ReturnStatement) ASTNode.copySubtree(ast, rstm);
    final IConfigBody body = ConfigBodyFactory.getConfigBody(rconf, 
        template.getTemplate(), rstm, ast);
    MethodDeclaration method = body.config();
    List<Type> types = ParameterTranslator.extractTypes(template.getVariables(), ast);
    method = ParameterTranslator.addParameter(types, template.getHoles(), 
        rconf.getRefasterRule(), method);
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
  private static Template getTemplate(final ReturnStatementConfig rconfig) 
          throws BadLocationException, IOException, NoFilepatternException, GitAPIException {
    final String commit = rconfig.getCommit();
    CommitUtils.checkoutIfDiffer(commit, rconfig.getPi());
    final String file = rconfig.getPath();
    final AST ast = AST.newAST(AST.JLS8);
    final Tuple<List<ASTNode>, List<ASTNode>> holeAndSubstutings = 
        getHolesAndSubstutingTrees(rconfig.getTarget(), rconfig.getNodes(), ast);
    List<ASTNode> holeVariables = holeAndSubstutings.getItem1(); 
    List<ASTNode> substutings = holeAndSubstutings.getItem2();
    removeCouldNotBeAbstracted(ast, holeVariables, substutings);
    final Document document = rewrite(file, substutings, holeVariables, rconfig.getVersion());
    final String srcModified = document.get();
    final Tuple<TreeContext, TreeContext> baEdit = EditUtils.beforeAfterCxt(file, srcModified);
    final Tuple<CompilationUnit, CompilationUnit> cunit = 
        EditUtils.beforeAfter(file, srcModified, rconfig.getVersion());
    final DiffCalculator diff = new DiffTreeContext(baEdit.getItem1(), baEdit.getItem2());
    diff.diff();
    final ITree srcTree = diff.getSrc().getRoot();
    final ITree dstTree = diff.getDst().getRoot();
    IMatcher<ITree> match = new PositionTreeMatcher(rconfig.getTarget());
    MatchCalculator<ITree> mcalc = new TreeMatchCalculator(match);
    final ITree srcTarget = mcalc.getNode(srcTree);
    final Matcher matcher = diff.getMatcher();
    final ITree dstMatch = matcher.getMappings().getDst(srcTarget);
    if (dstMatch == null) {
      System.out.println("DEBUG: could not find match for: " + srcTarget);
      return null;
    }
    match = new PositionTreeMatcher(dstMatch);
    mcalc = new TreeMatchCalculator(match);
    final ITree dstTarget = mcalc.getNode(dstTree);
    IMatcher<ASTNode> nodematch = new PositionNodeMatcher(dstTarget);
    MatchCalculator<ASTNode> nodecalc = new NodeMatchCalculator(nodematch);
    final ASTNode dstAstNode = nodecalc.getNode(cunit.getItem2());
    return new Template(dstAstNode, substutings, holeVariables);
  }

  private static void removeCouldNotBeAbstracted(
      final AST ast, List<ASTNode> holeVariables, List<ASTNode> substutings) {
    try {
      List<ASTNode> toRemoveHoles = new ArrayList<>();
      List<ASTNode> toRemoveSubtt = new ArrayList<>();
      List<Integer> filtered = filterTypes(substutings);
      for (int i = 0; i < filtered.size(); i++) {
        toRemoveHoles.add(holeVariables.get(filtered.get(i)));
        toRemoveSubtt.add(substutings.get(filtered.get(i)));
      }
      List<ASTNode> newHoles = new ArrayList<>();
      List<ASTNode> newSubs = new ArrayList<>();
      for (int i = 0; i < substutings.size(); i++) {
        boolean toRemove = false;
        for (int j = 0; j < toRemoveSubtt.size(); j++) {
          if (substutings.get(i).toString().equals(
              toRemoveSubtt.get(j).toString())) {
            toRemove = true;
          }
        }
        if (!toRemove) {
          newHoles.add(holeVariables.get(i));
          newSubs.add(substutings.get(i));
        }
      }
      holeVariables.clear();
      holeVariables.addAll(newHoles);
      substutings.clear();
      substutings.addAll(newSubs);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Filter types.
   * @param nodes to be filtered
   * @return index of nodes to be removed
   */
  public static List<Integer> filterTypes(List<ASTNode> nodes) {
    try {
      List<Integer> toRemove = new ArrayList<>();
      for (int i = 0; i < nodes.size(); i++) {
        if (!(nodes.get(i) instanceof Type)) {
          continue;
        }
        Type tmp = (Type) nodes.get(i);
        if (tmp.isParameterizedType() || tmp.isPrimitiveType() || tmp.isArrayType() 
            || tmp.isAnnotatable() || tmp.isIntersectionType() || tmp.isNameQualifiedType() 
            || tmp.isSimpleType() || tmp.isQualifiedType() || tmp.isUnionType() 
            || tmp.isWildcardType()) {
          toRemove.add(i);
        }
      }
      return toRemove;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets substituting trees associated to each role.
   * @param target target tree.
   * @param substitutingTrees substituting trees.
   * @param ast abstract syntax tree being edited
   * @param names to store the name of the hole variables.
   */
  private static Tuple<List<ASTNode>, List<ASTNode>> getHolesAndSubstutingTrees(
      final ASTNode target, final List<ASTNode> substitutingTrees, final AST ast) {
    final List<ASTNode> holeVariables = new ArrayList<ASTNode>();
    final List<ASTNode> targets = new ArrayList<>();
    for (int i = 0; i < substitutingTrees.size(); i++) {
      final ASTNode node = substitutingTrees.get(i);
      if (node == null) {
        continue;
      }
      //create a variable
      final SimpleName holeVariable = ast.newSimpleName("v_" + i);
      //nodes associated to the variable
      List<ASTNode> nodes = getNodesSameName(target, node.toString());
      for (final ASTNode nodek : nodes) {
        holeVariables.add(holeVariable);
        targets.add(nodek);
      }
    }
    return new Tuple<>(holeVariables, targets);
  }
  
  /**
   * Get nodes with same name.
   * @param target target where the nodes will be searched for.
   * @param nodeContent content of the node to be searched for.
   */
  private static List<ASTNode> getNodesSameName(final ASTNode target, final String nodeContent) {
    final IMatcher<ASTNode> match = new ValueNodeMatcher(nodeContent);
    final MatchCalculator<ASTNode> mcal = new NodeMatchCalculator(match);
    return mcal.getNodes(target);
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
  private static Document rewrite(final String file, final List<ASTNode> substutings, 
      final List<ASTNode> holeVariables, final Version version) throws BadLocationException {
    final String [] sources = version.getSource();
    final String [] classpath = version.getClasspath();
    final JParser srcParser = new JParser();
    final CompilationUnit root = srcParser.parseWithDocument(file, sources, classpath);
    final Document document = srcParser.getDocument();
    final ASTRewrite rewriter = ASTRewrite.create(root.getAST());
    root.recordModifications();
    // edit the source code
    for (int i = 0; i < substutings.size(); i++) {
      final ASTNode source = substutings.get(i);
      final ASTNode holeVariable = holeVariables.get(i);
      IMatcher<ASTNode> match = new PositionNodeMatcher(source);
      final MatchCalculator<ASTNode> mcalc = new NodeMatchCalculator(match);
      final ASTNode srcTargetNode = mcalc.getNode(root);
      rewriter.replace(srcTargetNode, holeVariable, null);
    }
    final TextEdit edit = rewriter.rewriteAST(document, null);
    edit.apply(document);
    return document;
  }

}
