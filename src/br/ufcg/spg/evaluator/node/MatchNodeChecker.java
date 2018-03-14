package br.ufcg.spg.evaluator.node;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.project.ProjectAnalyzer;
import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.replacement.Replacement;
import br.ufcg.spg.replacement.ReplacementUtils;

import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Checks mapping.
 */
public class MatchNodeChecker implements INodeChecker {
  /**
   * Source code edit.
   */
  private final transient Edit srcEdit;
  /**
   * Destination code edit.
   */
  private final transient Edit dstEdit;
  /**
   * List of source code replacement.
   */
  private transient List<Replacement<ASTNode>> src;
  /**
   * List of destination code replacement.
   */
  private transient List<Replacement<ASTNode>> dst;
  
  
  /**
   * Creates a new instance.
   * @param srcEdit source edit
   * @param dstEdit destination edit
   * @param src source replacement
   * @param dst destination source
   */
  public MatchNodeChecker(final Edit srcEdit, final Edit dstEdit, 
      final List<Replacement<ASTNode>> src, 
      final List<Replacement<ASTNode>> dst) {
    this.srcEdit = srcEdit;
    this.dstEdit = dstEdit;
    this.src = src;
    this.dst = dst;
  }

  @Override
  public boolean check() {
    try {
      final ProjectInfo pi = ProjectAnalyzer.project(srcEdit);
      final Tuple<List<ASTNode>, List<ASTNode>> baList = ReplacementUtils.ba(srcEdit,
          dstEdit, pi, src, dst);
      final List<ASTNode> srcNodes = baList.getItem1();
      final List<ASTNode> dstNodes = baList.getItem2();
      for (int j = 0; j < srcNodes.size(); j++) {
        final String srcNode = srcNodes.get(j).toString();
        // no map is found on the after version (deletion)
        if (dstNodes.get(j) == null) {
          continue;
        }
        final String dstNode = dstNodes.get(j).toString();
        // before after node differs
        if (!srcNode.equals(dstNode)) {
          return false;
        }
      }
      return true;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
