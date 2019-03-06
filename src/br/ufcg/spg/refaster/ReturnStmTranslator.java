package br.ufcg.spg.refaster;

import br.ufcg.spg.bean.Template;
import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.diff.DiffCalculator;
import br.ufcg.spg.diff.DiffTreeContext;
import br.ufcg.spg.edit.EditUtils;
import br.ufcg.spg.git.CommitUtils;
import br.ufcg.spg.node.NodeFinder;
import br.ufcg.spg.project.Version;
import br.ufcg.spg.refaster.config.ReturnStatementConfig;
import br.ufcg.spg.refaster.config.TransformationConfigObject;
import br.ufcg.spg.replacement.Replacement;
import br.ufcg.spg.replacement.ReplacementUtils;
import br.ufcg.spg.rewrite.RewriterUtils;
import br.ufcg.spg.type.TypeUtils;

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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;

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
        config.getBa().getItem1(), config.getPi(),
        config, befores);
    ReturnStatementConfig aconfig = getReturnStatementConfig(
        config.getCommit(), config.getDstPath(),
        config.getNodeDst(), config.getDstList(),
        config.getBa().getItem2(), config.getPi(),
        config, afters);
    final Type returnType = TypeUtils.extractType(bconfig.getTarget(), bconfig.getRefasterRule().getAST());
    MethodDeclaration before = addReturnStatement(bconfig, returnType);
    MethodDeclaration after = addReturnStatement(aconfig, returnType);
    return new Tuple<>(before, after);
  }

  /**
   * Create a return statement configuration.
   * @param target target node
   * @param targetList nodes in target.
   * @param m method in Refaster rul
   * @param config configuration
   * @param nodes list of nodes.
   */
  private static ReturnStatementConfig getReturnStatementConfig(
      final String commit, final String path,
      ASTNode target, List<Replacement<ASTNode>> targetList, 
      MethodDeclaration m, String project,
      TransformationConfigObject config, final List<ASTNode> nodes) {
    ReturnStatementConfig rconfig = new ReturnStatementConfig();
    rconfig.setCommit(commit);
    rconfig.setPath(path);
    rconfig.setTarget(target);
    rconfig.setNodes(nodes);
    rconfig.setTargetList(targetList);
    rconfig.setRefasterRule(config.getRefasterRule());
    rconfig.setMethod(m);
    //rconfig.setVersion(project);
    rconfig.setPi(config.getPi());
    return rconfig;
  }
  
  /**
   * Add a return statement to a method body.
   * @param rconf return statement configuration.
   */
  private static MethodDeclaration addReturnStatement(ReturnStatementConfig rconf, Type returnType) 
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
    List<Type> types = TypeUtils.extractTypes(template.getVariables(), ast);
    method = ParameterTranslator.addParameter(types, template.getHoles(), 
        rconf.getRefasterRule(), method);
    method = body.configReturnType(returnType, rconf.getRefasterRule(), method);
    return method;
  }
  
  /**
   * gets the node edited by replacing the node by the anti-unification
   * variable.
   * 
   * @param rconfig
   *          return configuration object
   */
  private static Template getTemplate(final ReturnStatementConfig rconfig) 
          throws BadLocationException {
    //final String commit = rconfig.getCommit();
    //CommitUtils.checkoutIfDiffer(commit, rconfig.getPi());
    final String file = rconfig.getPath();
    final AST ast = AST.newAST(AST.JLS8);
    final Tuple<List<ASTNode>, List<ASTNode>> holeAndSubstutings = 
        getHolesAndSubstutingTrees(rconfig.getTarget(), rconfig.getNodes(), ast);
    List<ASTNode> holeVariables = holeAndSubstutings.getItem1(); 
    List<ASTNode> substutings = holeAndSubstutings.getItem2();
    removeCouldNotBeAbstracted(ast, holeVariables, substutings);
    final Document document = RewriterUtils.rewrite(
        file, substutings, holeVariables);
    final String srcModified = document.get();
    final Tuple<TreeContext, TreeContext> baEdit = EditUtils.beforeAfterCxt(file, srcModified);
    final Tuple<CompilationUnit, CompilationUnit> cunit = 
        EditUtils.beforeAfter(file, srcModified);
    final DiffCalculator diff = new DiffTreeContext(baEdit.getItem1(), baEdit.getItem2());
    diff.diff();
    final ITree dstTree = diff.getDst().getRoot();
    final ITree dstMatch = RefasterUtils.getMatch(rconfig, diff);
    if (dstMatch == null) {
      System.out.println("DEBUG: could not find match for: " + rconfig.getTarget());
      return null;
    }
    final ASTNode dstAstNode = NodeFinder.getNode(cunit.getItem2(), dstTree, dstMatch);
    return new Template(dstAstNode, substutings, holeVariables);
  }

  private static void removeCouldNotBeAbstracted(
      final AST ast, List<ASTNode> holeVariables, List<ASTNode> substutings) {
    try {
      List<ASTNode> toRemoveHoles = new ArrayList<>();
      List<ASTNode> toRemoveSubtt = new ArrayList<>();
      List<Integer> filtered = TypeUtils.filterTypes(substutings);
      for (int i = 0; i < filtered.size(); i++) {
        toRemoveHoles.add(holeVariables.get(filtered.get(i)));
        toRemoveSubtt.add(substutings.get(filtered.get(i)));
      }
      removeNodes(holeVariables, substutings, toRemoveSubtt);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void removeNodes(List<ASTNode> holeVariables, 
      List<ASTNode> substutings, List<ASTNode> toRemoveSubtt) {
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
  }
  
  /**
   * Gets substituting trees associated to each role.
   * @param target target tree.
   * @param substitutingTrees substituting trees.
   * @param ast abstract syntax tree being edited
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
      List<ASTNode> nodes = NodeFinder.getNodesSameName(target, node.toString());
      for (final ASTNode nodek : nodes) {
        holeVariables.add(holeVariable);
        targets.add(nodek);
      }
    }
    return new Tuple<>(holeVariables, targets);
  }

}
