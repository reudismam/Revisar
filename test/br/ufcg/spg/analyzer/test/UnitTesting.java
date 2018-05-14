package br.ufcg.spg.analyzer.test;

import br.ufcg.spg.node.NodesExtractor;

import at.jku.risc.stout.urauc.algo.JustificationException;
import at.jku.risc.stout.urauc.util.ControlledException;
import br.ufcg.spg.main.MainArguments;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.path.PathUtils;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
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

  private void configMainArguments() {
    final MainArguments arguments = MainArguments.getInstance();
    arguments.setProjects("projects.txt");
    arguments.setProjectFolder("../Projects");
  }
}