// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static nl.hannahsten.texifyidea.psi.LatexTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

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
    if (t instanceof IFileElementType) {
      r = parse_root_(t, b, 0);
    }
    else {
      r = false;
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return latexFile(b, l + 1);
  }

  /* ********************************************************** */
  // BEGIN_TOKEN STAR? parameter*
  public static boolean begin_command(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "begin_command")) return false;
    if (!nextTokenIs(b, BEGIN_TOKEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, BEGIN_COMMAND, null);
    r = consumeToken(b, BEGIN_TOKEN);
    p = r; // pin = 1
    r = r && report_error_(b, begin_command_1(b, l + 1));
    r = p && begin_command_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // STAR?
  private static boolean begin_command_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "begin_command_1")) return false;
    consumeToken(b, STAR);
    return true;
  }

  // parameter*
  private static boolean begin_command_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "begin_command_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameter(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "begin_command_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // COMMAND_TOKEN STAR? parameter*
  public static boolean commands(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "commands")) return false;
    if (!nextTokenIs(b, COMMAND_TOKEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, COMMANDS, null);
    r = consumeToken(b, COMMAND_TOKEN);
    p = r; // pin = 1
    r = r && report_error_(b, commands_1(b, l + 1));
    r = p && commands_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // STAR?
  private static boolean commands_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "commands_1")) return false;
    consumeToken(b, STAR);
    return true;
  }

  // parameter*
  private static boolean commands_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "commands_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameter(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "commands_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // COMMENT_TOKEN
  public static boolean comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comment")) return false;
    if (!nextTokenIs(b, COMMENT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMENT_TOKEN);
    exit_section_(b, m, COMMENT, r);
    return r;
  }

  /* ********************************************************** */
  // no_math_content
  public static boolean content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONTENT, "<content>");
    r = no_math_content(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // DISPLAY_MATH_START math_content? DISPLAY_MATH_END
  public static boolean display_math(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "display_math")) return false;
    if (!nextTokenIs(b, DISPLAY_MATH_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DISPLAY_MATH, null);
    r = consumeToken(b, DISPLAY_MATH_START);
    p = r; // pin = 1
    r = r && report_error_(b, display_math_1(b, l + 1));
    r = p && consumeToken(b, DISPLAY_MATH_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // math_content?
  private static boolean display_math_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "display_math_1")) return false;
    math_content(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // END_TOKEN STAR? parameter*
  public static boolean end_command(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "end_command")) return false;
    if (!nextTokenIs(b, END_TOKEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, END_COMMAND, null);
    r = consumeToken(b, END_TOKEN);
    p = r; // pin = 1
    r = r && report_error_(b, end_command_1(b, l + 1));
    r = p && end_command_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // STAR?
  private static boolean end_command_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "end_command_1")) return false;
    consumeToken(b, STAR);
    return true;
  }

  // parameter*
  private static boolean end_command_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "end_command_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameter(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "end_command_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // begin_command environment_content? end_command
  public static boolean environment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "environment")) return false;
    if (!nextTokenIs(b, BEGIN_TOKEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENVIRONMENT, null);
    r = begin_command(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, environment_1(b, l + 1));
    r = p && end_command(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // environment_content?
  private static boolean environment_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "environment_1")) return false;
    environment_content(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // content+
  public static boolean environment_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "environment_content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENVIRONMENT_CONTENT, "<environment content>");
    r = content(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "environment_content", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // OPEN_BRACE content* CLOSE_BRACE
  public static boolean group(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group")) return false;
    if (!nextTokenIs(b, OPEN_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GROUP, null);
    r = consumeToken(b, OPEN_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, group_1(b, l + 1));
    r = p && consumeToken(b, CLOSE_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // content*
  private static boolean group_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "group_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // INLINE_MATH_START math_content? INLINE_MATH_END
  public static boolean inline_math(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math")) return false;
    if (!nextTokenIs(b, INLINE_MATH_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INLINE_MATH, null);
    r = consumeToken(b, INLINE_MATH_START);
    p = r; // pin = 1
    r = r && report_error_(b, inline_math_1(b, l + 1));
    r = p && consumeToken(b, INLINE_MATH_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // math_content?
  private static boolean inline_math_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_1")) return false;
    math_content(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // content*
  static boolean latexFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "latexFile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "latexFile", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // no_math_content+
  public static boolean math_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "math_content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MATH_CONTENT, "<math content>");
    r = no_math_content(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!no_math_content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "math_content", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // inline_math | display_math
  public static boolean math_environment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "math_environment")) return false;
    if (!nextTokenIs(b, "<math environment>", DISPLAY_MATH_START, INLINE_MATH_START)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MATH_ENVIRONMENT, "<math environment>");
    r = inline_math(b, l + 1);
    if (!r) r = display_math(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // comment | environment | math_environment | commands | group | open_group | OPEN_PAREN | CLOSE_PAREN | M_OPEN_BRACKET | M_CLOSE_BRACKET | normal_text
  public static boolean no_math_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "no_math_content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NO_MATH_CONTENT, "<no math content>");
    r = comment(b, l + 1);
    if (!r) r = environment(b, l + 1);
    if (!r) r = math_environment(b, l + 1);
    if (!r) r = commands(b, l + 1);
    if (!r) r = group(b, l + 1);
    if (!r) r = open_group(b, l + 1);
    if (!r) r = consumeToken(b, OPEN_PAREN);
    if (!r) r = consumeToken(b, CLOSE_PAREN);
    if (!r) r = consumeToken(b, M_OPEN_BRACKET);
    if (!r) r = consumeToken(b, M_CLOSE_BRACKET);
    if (!r) r = normal_text(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (NORMAL_TEXT_WORD | STAR)+
  public static boolean normal_text(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_text")) return false;
    if (!nextTokenIs(b, "<normal text>", NORMAL_TEXT_WORD, STAR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NORMAL_TEXT, "<normal text>");
    r = normal_text_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!normal_text_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "normal_text", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // NORMAL_TEXT_WORD | STAR
  private static boolean normal_text_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_text_0")) return false;
    boolean r;
    r = consumeToken(b, NORMAL_TEXT_WORD);
    if (!r) r = consumeToken(b, STAR);
    return r;
  }

  /* ********************************************************** */
  // OPEN_BRACKET content* CLOSE_BRACKET
  public static boolean open_group(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "open_group")) return false;
    if (!nextTokenIs(b, OPEN_BRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OPEN_GROUP, null);
    r = consumeToken(b, OPEN_BRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, open_group_1(b, l + 1));
    r = p && consumeToken(b, CLOSE_BRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // content*
  private static boolean open_group_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "open_group_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "open_group_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // open_group
  public static boolean optional_param(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_param")) return false;
    if (!nextTokenIs(b, OPEN_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = open_group(b, l + 1);
    exit_section_(b, m, OPTIONAL_PARAM, r);
    return r;
  }

  /* ********************************************************** */
  // optional_param | required_param
  public static boolean parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter")) return false;
    if (!nextTokenIs(b, "<parameter>", OPEN_BRACE, OPEN_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER, "<parameter>");
    r = optional_param(b, l + 1);
    if (!r) r = required_param(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // group
  public static boolean required_param(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_param")) return false;
    if (!nextTokenIs(b, OPEN_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = group(b, l + 1);
    exit_section_(b, m, REQUIRED_PARAM, r);
    return r;
  }

}
