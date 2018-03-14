package br.ufcg.spg.mapper;

public class AsciiMapper {
  /**
   * Gets description of a character.
   * @param c character to describe
   * @return string describing character.
   */
  public static String descs(final char c) {
    if ('A' <= c && c <= 'Z') {
      return "upper case " + String.valueOf(c);
    } else if ('a' <= c && c <= 'z') {
      return "lower case " + String.valueOf(c);
    } else if ('0' <= c && c <= '9') {
      return "digit " + String.valueOf(c);
    } else {
      switch (c) {
        case 32 :
          return "space";
        case 33 :
          return "exclamation";
        case 34 :
          return "quote";
        case 35 :
          return "sharp";
        case 36 :
          return "dollar_sign";
        case 37 :
          return "percent";
        case 38 :
          return "ampersand";
        case 39 :
          return "apostrophe";
        case 40 :
          return "left_parenthesis";
        case 41 :
          return "right_parenthesis";
        case 42 :
          return "asterisk";
        case 43 :
          return "plus";
        case 44 :
          return "comma";
        case 45 :
          return "minus";
        case 46 :
          return "period";
        case 47 :
          return "slash";
        case 58 :
          return "colon";
        case 59 :
          return "semicolon";
        case 60 :
          return "less_than";
        case 61 :
          return "equals";
        case 62 :
          return "greater_than";
        case 63 :
          return "question_mark";
        case 64 :
          return "at_sign";
        case 91 :
          return "left_square_bracket";
        case 92 :
          return "backslash";
        case 93 :
          return "right_square_bracket";
        case 94 :
          return "circumflex";
        case 95 :
          return "underscore";
        case 96 :
          return "grave";
        case 123 :
          return "left_curly_brace";
        case 124 :
          return "vertical_bar";
        case 125 :
          return "right_curly_brace";
        case 126 :
          return "tilde";
        default :
          return "_CHAR";
      }
    }
  }
}
