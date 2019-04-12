package br.ufcg.spg.util;

import br.ufcg.spg.antiunification.AntiUnifier;
import br.ufcg.spg.tree.RevisarTree;
import io.bretty.console.tree.TreeNodeConverter;
import io.bretty.console.tree.TreePrinter;

import java.util.List;

public class PrintUtils {
  
  private PrintUtils() {
  }

  public static String prettyPrint(AntiUnifier au) {
    return prettyPrintATree(au);
  }

  /**
   * Pretty print of a tree.
   * 
   * @param atree
   *          ATree to be printed.
   */
  public static String prettyPrint(final RevisarTree<String> atree) {
    final RevisarTree<String> root = atree;
    final TreeNodeConverter<RevisarTree<String>> converter = 
        new TreeNodeConverter<RevisarTree<String>>() {
      @Override
      public String name(RevisarTree<String> tree) {
        return tree.getValue();
      }
      
      @Override
      public List<? extends RevisarTree<String>> children(RevisarTree<String> au) {
        return au.getChildren();
      }
    };
    return TreePrinter.toString(root, converter).trim();
  }

  /**
   * Pretty print an anti-unification tree.
   * 
   * @param au
   *          - ant-unification
   */
  private static String prettyPrintATree(AntiUnifier au) {
    RevisarTree<String> atree = au.toRevisarTree();
    return prettyPrint(atree);
  }
}
