package br.ufcg.spg.validator.template;

import br.ufcg.spg.analyzer.util.AnalyzerUtil;
import br.ufcg.spg.edit.Edit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Checks rules.
 */
public final class CompositeTemplateValidator implements ITemplateValidator {
  
  /**
   * List of rule each node must be checked in each node.
   */
  private final transient List<ITemplateValidator> rules;

  /**
   * Constructor
   * 
   * @param depth
   *          depth on the AST tree.
   * @param rules
   *          rules to be analyzed.
   */
  private CompositeTemplateValidator(final List<ITemplateValidator> rules) {
    this.rules = rules;
  }

  /**
   * Creates a instance of a rule checker.
   * @param srcEdits
   *          source code edits
   * @return a new instance of a rule checker.
   */
  public static CompositeTemplateValidator create(final String srcAu, 
      final String dstAu, final List<Edit> srcEdits) {
    final List<ITemplateValidator> rules = new ArrayList<>();
    final ITemplateValidator minvo = new MethodInvocationTemplateValidator(srcEdits);
    final String simpleTypeLabel = AnalyzerUtil.getLabel(ASTNode.SIMPLE_TYPE);
    final ITemplateValidator simpleType = new LabelTemplateValidator(srcAu, 
        srcEdits, simpleTypeLabel);
    final String primitiveTypeLabel = AnalyzerUtil.getLabel(ASTNode.PRIMITIVE_TYPE);
    final ITemplateValidator primitiveType = new LabelTemplateValidator(srcAu, 
        srcEdits, primitiveTypeLabel);
    final String markerAnnotationLabel = AnalyzerUtil.getLabel(ASTNode.MARKER_ANNOTATION);
    final ITemplateValidator markerAnnotation = new LabelTemplateValidator(srcAu, 
        srcEdits, markerAnnotationLabel);
    final ITemplateValidator methodName = new MethodInvocationNameValidator(srcEdits);
    rules.add(methodName);
    rules.add(minvo);
    rules.add(simpleType);
    rules.add(primitiveType);
    rules.add(markerAnnotation);
    return new CompositeTemplateValidator(rules);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValidUnification() {
    try {
      for (final ITemplateValidator rule : rules) {
        if (!rule.isValidUnification()) {
          return false;
        }
      }
      return true;
    } catch (final Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
