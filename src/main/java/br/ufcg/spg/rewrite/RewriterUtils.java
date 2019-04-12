package br.ufcg.spg.rewrite;

import br.ufcg.spg.matcher.IMatcher;
import br.ufcg.spg.matcher.PositionNodeMatcher;
import br.ufcg.spg.matcher.calculator.MatchCalculator;
import br.ufcg.spg.matcher.calculator.NodeMatchCalculator;
import br.ufcg.spg.parser.JParser;
import br.ufcg.spg.project.Version;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

public class RewriterUtils {

  /**
   * gets the node edited by replacing the node by the anti-unification
   * variable.
   */
  public static Document rewrite(final String file, final List<ASTNode> substutings, 
      final List<ASTNode> holeVariables) throws BadLocationException {
    //final String [] sources = version.getSource();
    //final String [] classpath = version.getClasspath();
    final JParser srcParser = new JParser();
    final CompilationUnit root = srcParser.parseWithDocument(file);
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
