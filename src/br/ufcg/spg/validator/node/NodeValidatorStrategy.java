package br.ufcg.spg.validator.node;

import br.ufcg.spg.compile.CompilerUtils;
import br.ufcg.spg.edit.Edit;
import br.ufcg.spg.project.ProjectAnalyzer;
import br.ufcg.spg.project.ProjectInfo;
import br.ufcg.spg.project.Version;
import br.ufcg.spg.replacement.Replacement;
import br.ufcg.spg.replacement.ReplacementUtils;
import br.ufcg.spg.validator.template.ITransformationValidatorStrategy;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Class for transformation evaluation based on AST Node.
 *
 */
public class NodeValidatorStrategy implements ITransformationValidatorStrategy {

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValidTrans(final List<Edit> srcEdits, final String srcAu, final String dstAu) {
    final Map<String, List<String>> dstMap = new Hashtable<>();
    try {
      // Since all edits are consistent in the cluster, we only need two edits.
      INodeChecker ch = RuleNodeChecker.create(0, srcEdits);
      if (!ch.isValidUnification()) {
        return false;
      }
      for (int i = 0; i < srcEdits.size(); i++) {
        final Edit srcEdit = srcEdits.get(i);
        final Edit dstEdit = srcEdit.getDst();
        final String commit = dstEdit.getCommit();
        final ProjectInfo pi = ProjectAnalyzer.project(srcEdit);
        final Version srcVersion = pi.getSrcVersion();
        final Version dstVersion = pi.getDstVersion();
        final CompilationUnit srcUnit = CompilerUtils.getCunit(srcEdit, commit, srcVersion, pi);
        final List<Replacement<ASTNode>> src = ReplacementUtils.replacements(srcEdit, srcAu, srcUnit);
        final CompilationUnit dstUnit = CompilerUtils.getCunit(dstEdit, commit, dstVersion, pi);
        final List<Replacement<ASTNode>> dst = ReplacementUtils.replacements(dstEdit, dstAu, dstUnit);
        for (final Replacement<ASTNode> re : dst) {
          if (!dstMap.containsKey(re.getUnification())) {
            dstMap.put(re.getUnification(), new ArrayList<>());
          }
          dstMap.get(re.getUnification()).add(re.getNode().toString());
        }
        ch = new MatchNodeChecker(srcEdit, dstEdit, src, dst);
        final boolean ba = ch.isValidUnification();
        if (!ba) {
          return ba;
        }
      }
      return true;
      // ch = new UnmappedDstChecker(srcEdits, srcAu, dstAu, dstMap);
      // return ch.check();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
