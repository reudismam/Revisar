package br.ufcg.spg.constraint;

import br.ufcg.spg.constraint.rule.Rule1;
import br.ufcg.spg.constraint.rule.Rule11;
import br.ufcg.spg.constraint.rule.Rule12;
import br.ufcg.spg.constraint.rule.Rule13;
import br.ufcg.spg.constraint.rule.Rule14;
import br.ufcg.spg.constraint.rule.Rule15;
import br.ufcg.spg.constraint.rule.Rule16;
import br.ufcg.spg.constraint.rule.Rule17;
import br.ufcg.spg.constraint.rule.Rule18;
import br.ufcg.spg.constraint.rule.Rule2;
import br.ufcg.spg.constraint.rule.Rule3;
import br.ufcg.spg.constraint.rule.Rule4;
import br.ufcg.spg.constraint.rule.Rule5;
import br.ufcg.spg.constraint.rule.Rule6;
import br.ufcg.spg.constraint.rule.Rule7;
import br.ufcg.spg.constraint.rule.Rule8;
import br.ufcg.spg.constraint.rule.RuleBase;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public class ConstraintFactory {
  /**
   * Gets the constraint rule suitable for each node.
   * @param node - node
   * @return the constraint rule suitable for each node.
   */
  public static List<IConstraintRule> getConstraints(final ASTNode node) {
    final RuleBase rule1 = new Rule1();
    final RuleBase rule2 = new Rule2();
    final RuleBase rule3 = new Rule3();
    final RuleBase rule4 = new Rule4();
    final RuleBase rule5 = new Rule5();
    final RuleBase rule6 = new Rule6();
    final RuleBase rule7 = new Rule7();
    final RuleBase rule8 = new Rule8();
    final RuleBase rule11 = new Rule11();
    final RuleBase rule12 = new Rule12();
    final RuleBase rule13 = new Rule13();
    final RuleBase rule14 = new Rule14();
    final RuleBase rule15 = new Rule15();
    final RuleBase rule16 = new Rule16();
    final RuleBase rule17 = new Rule17();
    final RuleBase rule18 = new Rule18();
    final RuleBase rule19 = new Rule18();
    final List<RuleBase> rules = new ArrayList<>();
    rules.add(rule1);
    rules.add(rule2);
    rules.add(rule3);
    rules.add(rule4);
    rules.add(rule5);
    rules.add(rule6);
    rules.add(rule7);
    rules.add(rule8);
    rules.add(rule11);
    rules.add(rule12);
    rules.add(rule13);
    rules.add(rule14);
    rules.add(rule15);
    rules.add(rule16);
    rules.add(rule17);
    rules.add(rule18);
    rules.add(rule19);
    final List<IConstraintRule> valid = new ArrayList<>();
    for (final RuleBase rule : rules) {
      if (rule.isApplicableTo(node)) {
        valid.add(rule);
      }
    }
    return valid;
  }
}
