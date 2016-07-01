// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;

import static com.intellij.lang.parser.GeneratedParserUtilBase.*;

import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import nl.rubensten.texifyidea.psi.LatexTypes;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class LatexParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == LatexTypes.COMMAND) {
      r = command(b, 0);
    }
    else if (t == LatexTypes.COMMENT) {
      r = comment(b, 0);
    }
    else if (t == LatexTypes.CONTENT) {
      r = content(b, 0);
    }
    else if (t == LatexTypes.DISPLAY_MATH) {
      r = display_math(b, 0);
    }
    else if (t == LatexTypes.GROUP) {
      r = group(b, 0);
    }
    else if (t == LatexTypes.INLINE_MATH) {
      r = inline_math(b, 0);
    }
    else if (t == LatexTypes.MATH_ENVIRONMENT) {
      r = math_environment(b, 0);
    }
    else if (t == LatexTypes.NO_MATH_CONTENT) {
      r = no_math_content(b, 0);
    }
    else if (t == LatexTypes.OPEN_GROUP) {
      r = open_group(b, 0);
    }
    else if (t == LatexTypes.OPTIONAL_PARAM) {
      r = optional_param(b, 0);
    }
    else if (t == LatexTypes.PARAMETER) {
      r = parameter(b, 0);
    }
    else if (t == LatexTypes.REQUIRED_PARAM) {
      r = required_param(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return latexFile(b, l + 1);
  }

  /* ********************************************************** */
  // COMMAND_TOKEN STAR? parameter*
  public static boolean command(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, LatexTypes.COMMAND_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, LatexTypes.COMMAND_TOKEN);
    r = r && command_1(b, l + 1);
    r = r && command_2(b, l + 1);
    exit_section_(b, m, LatexTypes.COMMAND, r);
    return r;
  }

  // STAR?
  private static boolean command_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command_1")) return false;
    GeneratedParserUtilBase.consumeToken(b, LatexTypes.STAR);
    return true;
  }

  // parameter*
  private static boolean command_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!parameter(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "command_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // COMMENT_TOKEN
  public static boolean comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comment")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, LatexTypes.COMMENT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, LatexTypes.COMMENT_TOKEN);
    exit_section_(b, m, LatexTypes.COMMENT, r);
    return r;
  }

  /* ********************************************************** */
  // no_math_content | math_environment
  public static boolean content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LatexTypes.CONTENT, "<content>");
    r = no_math_content(b, l + 1);
    if (!r) r = math_environment(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // DISPLAY_MATH_START (no_math_content)* DISPLAY_MATH_END
  public static boolean display_math(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "display_math")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, LatexTypes.DISPLAY_MATH_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, LatexTypes.DISPLAY_MATH_START);
    r = r && display_math_1(b, l + 1);
    r = r && GeneratedParserUtilBase.consumeToken(b, LatexTypes.DISPLAY_MATH_END);
    exit_section_(b, m, LatexTypes.DISPLAY_MATH, r);
    return r;
  }

  // (no_math_content)*
  private static boolean display_math_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "display_math_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!display_math_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "display_math_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (no_math_content)
  private static boolean display_math_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "display_math_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = no_math_content(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // OPEN_BRACE content* CLOSE_BRACE
  public static boolean group(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, LatexTypes.OPEN_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, LatexTypes.OPEN_BRACE);
    r = r && group_1(b, l + 1);
    r = r && GeneratedParserUtilBase.consumeToken(b, LatexTypes.CLOSE_BRACE);
    exit_section_(b, m, LatexTypes.GROUP, r);
    return r;
  }

  // content*
  private static boolean group_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "group_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // INLINE_MATH_DELIM (no_math_content)* INLINE_MATH_DELIM
  public static boolean inline_math(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, LatexTypes.INLINE_MATH_DELIM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, LatexTypes.INLINE_MATH_DELIM);
    r = r && inline_math_1(b, l + 1);
    r = r && GeneratedParserUtilBase.consumeToken(b, LatexTypes.INLINE_MATH_DELIM);
    exit_section_(b, m, LatexTypes.INLINE_MATH, r);
    return r;
  }

  // (no_math_content)*
  private static boolean inline_math_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!inline_math_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inline_math_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (no_math_content)
  private static boolean inline_math_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = no_math_content(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // content*
  static boolean latexFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "latexFile")) return false;
    int c = current_position_(b);
    while (true) {
      if (!content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "latexFile", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // inline_math | display_math
  public static boolean math_environment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "math_environment")) return false;
    if (!nextTokenIs(b, "<math environment>", LatexTypes.DISPLAY_MATH_START, LatexTypes.INLINE_MATH_DELIM)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LatexTypes.MATH_ENVIRONMENT, "<math environment>");
    r = inline_math(b, l + 1);
    if (!r) r = display_math(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // comment | command | group | open_group | NORMAL_TEXT
  public static boolean no_math_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "no_math_content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LatexTypes.NO_MATH_CONTENT, "<no math content>");
    r = comment(b, l + 1);
    if (!r) r = command(b, l + 1);
    if (!r) r = group(b, l + 1);
    if (!r) r = open_group(b, l + 1);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, LatexTypes.NORMAL_TEXT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // OPEN_BRACKET content* CLOSE_BRACKET
  public static boolean open_group(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "open_group")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, LatexTypes.OPEN_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, LatexTypes.OPEN_BRACKET);
    r = r && open_group_1(b, l + 1);
    r = r && GeneratedParserUtilBase.consumeToken(b, LatexTypes.CLOSE_BRACKET);
    exit_section_(b, m, LatexTypes.OPEN_GROUP, r);
    return r;
  }

  // content*
  private static boolean open_group_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "open_group_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "open_group_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // open_group
  public static boolean optional_param(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_param")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, LatexTypes.OPEN_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = open_group(b, l + 1);
    exit_section_(b, m, LatexTypes.OPTIONAL_PARAM, r);
    return r;
  }

  /* ********************************************************** */
  // optional_param | required_param
  public static boolean parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter")) return false;
    if (!nextTokenIs(b, "<parameter>", LatexTypes.OPEN_BRACE, LatexTypes.OPEN_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LatexTypes.PARAMETER, "<parameter>");
    r = optional_param(b, l + 1);
    if (!r) r = required_param(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // group
  public static boolean required_param(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_param")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, LatexTypes.OPEN_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = group(b, l + 1);
    exit_section_(b, m, LatexTypes.REQUIRED_PARAM, r);
    return r;
  }

}
