package br.ufcg.spg.util;

import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.tree.ATree;
import io.bretty.console.tree.TreeNodeConverter;
import io.bretty.console.tree.TreePrinter;

import java.util.List;

public class PrintUtils {

  public static String prettyPrint(AntiUnifier au) {
    return prettyPrintATree(au);
  }

  /**
   * Pretty print of a tree.
   * 
   * @param atree
   *          ATree to be printed.
   */
  public static String prettyPrint(ATree<String> atree) {
    ATree<String> root = atree;
    TreeNodeConverter<ATree<String>> converter = new TreeNodeConverter<ATree<String>>() {
      @Override
      public String name(ATree<String> tree) {
        return tree.getValue();
      }

      @Override
      public List<? extends ATree<String>> children(ATree<String> au) {
        return au.getChildren();
      }
    };
    String output = TreePrinter.toString(root, converter).trim();
    return output;
  }

  /**
   * Pretty print an anti-unification tree.
   * 
   * @param au
   *          - ant-unification
   */
  private static String prettyPrintATree(AntiUnifier au) {
    ATree<String> atree = au.toATree();
    String output = prettyPrint(atree);
    return output;
  }
}
