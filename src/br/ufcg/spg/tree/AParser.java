package br.ufcg.spg.tree;

public class AParser {
  /**
   * Input data to be traversed.
   */
  public static String data;

  /**
   * Current index.
   */
  public static int index;

  /**
   * Indicates whether there are more children to be analyzed.
   */
  public static boolean foundComma;

  /**
   * Parse the string representation to a tree representation.
   * 
   * @return tree representation
   */
  public static ATree<String> parser(final String dataInput) {
    if (dataInput.isEmpty()) {
      return new ATree<String>("");
    }
    data = dataInput;
    index = 0;
    try {
      final ATree<String> parsed = parser();
      if (data.trim().startsWith("(")) {
        return parsed.getChildren().get(0);
      }
      return parsed;
    } catch (final Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Parse the string representation to a tree representation.
   * 
   * @return tree representation
   */
  private static ATree<String> parser() {
    final ATree<String> tree = new ATree<String>("");
    tree.setPos(index);
    while (index < data.length()) {
      final char character = data.charAt(index);
      //start of a parent node
      if (character == '(') {
        index++;
        foundComma = false;
        final ATree<String> child = parser();
        tree.getChildren().add(child);
        child.setParent(tree);
      } else if (foundComma && character != ' ') {
        foundComma = false;
        final ATree<String> child = parser();
        tree.getChildren().add(child);
        child.setParent(tree);
      //end of node definition
      } else if (character == ')') {
        tree.setEnd(index);
        final String text = data.substring(tree.getPos(), index);
        tree.setStrValue(text);
        foundComma = false;
        return tree;
      } else if (character == ',') {
        tree.setEnd(index);
        final String text = data.substring(tree.getPos(), index);
        tree.setStrValue(text);
        index++;
        foundComma = true;
        return tree;
      } else {
        if (tree.getValue() == null) {
          tree.setValue("");
        }
        tree.setValue(tree.getValue() + data.charAt(index));
      }
      index++;
    }
    tree.setEnd(index - 1);
    final String text = data.substring(tree.getPos(), tree.getEnd());
    tree.setStrValue(text);
    return tree;
  }
}
