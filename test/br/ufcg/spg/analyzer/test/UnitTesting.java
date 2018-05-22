package br.ufcg.spg.analyzer.test;

import br.ufcg.spg.node.NodesExtractor;
import br.ufcg.spg.node.util.ASTNodeUtils;
import at.jku.risc.stout.urauc.algo.JustificationException;
import at.jku.risc.stout.urauc.util.ControlledException;
import br.ufcg.spg.main.MainArguments;
import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.KindNodeMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.NodeMatchCalculator;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.path.PathUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;

public class UnitTesting {
  
  @Test
  public void pathUtils() 
      throws IOException, JustificationException, ControlledException, CoreException {
    configMainArguments();
    String javaPath = "test\\br\\ufcg\\spg\\analyzer\\test\\TestSuite.java";
    CompilationUnit root = JParser.parse(javaPath);
    List<ASTNode> nodes = NodesExtractor.getNodes(root);
    for (ASTNode node : nodes) {
      String path = PathUtils.computePathRoot(node);
      ASTNode inRoot = PathUtils.getNode(root, path);
      assertEquals(node, inRoot);
    }
  }
  
  @Test
  public void getTop() 
      throws IOException, JustificationException, ControlledException, CoreException {
    configMainArguments();
    String javaPath = "test\\br\\ufcg\\spg\\analyzer\\test\\TestSuite.java";
    CompilationUnit root = JParser.parse(javaPath);
    IMatcher<ASTNode> matchMethods = new KindNodeMatcher(ASTNode.METHOD_DECLARATION);
    MatchCalculator<ASTNode> match = new NodeMatchCalculator(matchMethods);
    List<ASTNode> listMethods = match.getNodes(root);
    IMatcher<ASTNode> matchFields = new KindNodeMatcher(ASTNode.FIELD_DECLARATION);
    match = new NodeMatchCalculator(matchFields);
    List<ASTNode> listFields = match.getNodes(root);
    List<ASTNode> nodes = new ArrayList<>(listMethods);
    nodes.addAll(listFields);
    for (ASTNode node : nodes) {
      ASTNode top = ASTNodeUtils.getTopNode(node);
      assertTrue(top.getNodeType() == ASTNode.METHOD_DECLARATION || top.getNodeType() == ASTNode.FIELD_DECLARATION);
    }
  }

  private void configMainArguments() {
    final MainArguments arguments = MainArguments.getInstance();
    arguments.setProjects("projects.txt");
    arguments.setProjectFolder("../Projects");
  }
}