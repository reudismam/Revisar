package br.ufcg.spg.validator;

import br.ufcg.spg.bean.Tuple;
import br.ufcg.spg.compile.CompilerUtils;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.project.ProjectAnalyzer;
import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.project.Version;
import br.ufcg.spg.replacement.Replacement;
import br.ufcg.spg.replacement.ReplacementUtils;
import br.ufcg.spg.validator.node.INodeChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Unmapped edit checker.
 */
public class UnmappedDstChecker implements INodeChecker {
  
  /**
   * Source code edits.
   */
  private final transient List<Edit> srcEdits;
  
  /**
   * Anti-unification for source code.
   */
  private final transient String srcAu;
  
  /**
   * Anti-unification for destination code.
   */
  private final transient String dstAu;
  
  /**
   * Destination mapping.
   */
  private final transient Map<String, List<String>> dstMap;
  
  /**
   * Checks unmapped nodes.
   * @param srcEdits source code edit
   * @param srcAu anti-unification source cluster
   * @param dstAu anti-unification destination cluster
   * @param dstMap destination mapping.
   */
  public UnmappedDstChecker(final List<Edit> srcEdits, final String srcAu, final String dstAu,
      final Map<String, List<String>> dstMap) {
    super();
    this.srcEdits = srcEdits;
    this.srcAu = srcAu;
    this.dstAu = dstAu;
    this.dstMap = dstMap;
  }

  @Override
  public boolean isValidUnification() {
    try {
      final Edit srcEdit = srcEdits.get(0);
      final Edit dstEdit = srcEdit.getDst();
      final String commit = dstEdit.getCommit();
      final ProjectInfo pi = ProjectAnalyzer.project(srcEdit);
      final Version srcVersion = pi.getSrcVersion();
      final Version dstVersion = pi.getDstVersion();
      final CompilationUnit srcUnit = CompilerUtils.getCunit(srcEdit, commit, srcVersion, pi);
      final List<Replacement<ASTNode>> src = ReplacementUtils.replacements(srcEdit, srcAu, srcUnit);
      final CompilationUnit dstUnit = CompilerUtils.getCunit(dstEdit, commit, dstVersion, pi);
      final List<Replacement<ASTNode>> dst = ReplacementUtils.replacements(dstEdit, dstAu, dstUnit);
      final Tuple<List<ASTNode>, List<ASTNode>> baList = 
          ReplacementUtils.ba(srcEdit, dstEdit, pi, src,dst);
      final List<String> unmapped =  new ArrayList<>();
      for (final Replacement<ASTNode> re : dst) {
        final ASTNode reNode = re.getNode();
        boolean isMap = false;
        final String hash = re.getUnification();
        for (final ASTNode node : baList.getItem2()) {
          if (node == null) {
            continue;
          }
          if (reNode.getStartPosition() == node.getStartPosition() 
              && reNode.getLength() == node.getLength()) {
            isMap = true;
            break;
          }
        }
        if (!isMap) {
          unmapped.add(hash);
        }
      }
      for (final String key : unmapped) {
        final List<String> values = dstMap.get(key);
        for (final String value : values) {
          if (!value.equals(values.get(0))) {
            return false;
          }
        }
      }
      return true;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
