package br.ufcg.spg.string;

public class StringUtils {
  private StringUtils() {
  }

  private static final String returnpattern = "\n";

  /**
   * Print String side by side.
   */
  public static String printStringSideBySide(String turtle1, String turtle2) {
    // split the data into individual parts
    String[] one = turtle1.split(returnpattern);
    String[] two = turtle2.split(returnpattern);

    // find out the longest String in data set one
    int longestString = 0;
    for (String s : one) {
      if (longestString < s.length()) {
        longestString = s.length();
      }
    }

    // loop through parts and build new string
    StringBuilder b = new StringBuilder();
    int i;
    for (i = 0; i < one.length; i++) {
      for (int j = one[i].length(); j < longestString; j++) {
        one[i] += " ";
      }
      if (i < two.length) {
        b.append(one[i]).append(two[i]).append(returnpattern);
      } else {
        b.append(one[i]).append(returnpattern);
      }
    }
    if (i < two.length) {
      String left = "";
      for (int j = 0; j < longestString; j++) {
        left += " ";
      }
      for (int j = i; j < two.length; j++) {
        b.append(left).append(two[j]).append(returnpattern);
      }
    }
    // output
    return b.toString();
  }
}
