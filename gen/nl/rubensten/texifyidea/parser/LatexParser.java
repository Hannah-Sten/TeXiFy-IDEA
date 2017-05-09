// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static nl.rubensten.texifyidea.psi.LatexTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
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
    if (t == BEGIN_COMMAND) {
      r = begin_command(b, 0);
    }
    else if (t == COMMANDS) {
      r = commands(b, 0);
    }
    else if (t == COMMENT) {
      r = comment(b, 0);
    }
    else if (t == CONTENT) {
      r = content(b, 0);
    }
    else if (t == DISPLAY_MATH) {
      r = display_math(b, 0);
    }
    else if (t == END_COMMAND) {
      r = end_command(b, 0);
    }
    else if (t == ENVIRONMENT) {
      r = environment(b, 0);
    }
    else if (t == GROUP) {
      r = group(b, 0);
    }
    else if (t == INLINE_MATH) {
      r = inline_math(b, 0);
    }
    else if (t == MATH_ENVIRONMENT) {
      r = math_environment(b, 0);
    }
    else if (t == NO_MATH_CONTENT) {
      r = no_math_content(b, 0);
    }
    else if (t == OPEN_GROUP) {
      r = open_group(b, 0);
    }
    else if (t == OPTIONAL_PARAM) {
      r = optional_param(b, 0);
    }
    else if (t == PARAMETER) {
      r = parameter(b, 0);
    }
    else if (t == REQUIRED_PARAM) {
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
  // BEGIN_TOKEN STAR? parameter*
  public static boolean begin_command(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "begin_command")) return false;
    if (!nextTokenIs(b, BEGIN_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, BEGIN_TOKEN);
    r = r && begin_command_1(b, l + 1);
    r = r && begin_command_2(b, l + 1);
    exit_section_(b, m, BEGIN_COMMAND, r);
    return r;
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
    int c = current_position_(b);
    while (true) {
      if (!parameter(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "begin_command_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // COMMAND_TOKEN STAR? parameter*
  public static boolean commands(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "commands")) return false;
    if (!nextTokenIs(b, COMMAND_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMAND_TOKEN);
    r = r && commands_1(b, l + 1);
    r = r && commands_2(b, l + 1);
    exit_section_(b, m, COMMANDS, r);
    return r;
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
    int c = current_position_(b);
    while (true) {
      if (!parameter(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "commands_2", c)) break;
      c = current_position_(b);
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
  // no_math_content | math_environment
  public static boolean content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONTENT, "<content>");
    r = no_math_content(b, l + 1);
    if (!r) r = math_environment(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // DISPLAY_MATH_START (M_OPEN_BRACKET | M_CLOSE_BRACKET | no_math_content)* DISPLAY_MATH_END
  public static boolean display_math(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "display_math")) return false;
    if (!nextTokenIs(b, DISPLAY_MATH_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DISPLAY_MATH_START);
    r = r && display_math_1(b, l + 1);
    r = r && consumeToken(b, DISPLAY_MATH_END);
    exit_section_(b, m, DISPLAY_MATH, r);
    return r;
  }

  // (M_OPEN_BRACKET | M_CLOSE_BRACKET | no_math_content)*
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

  // M_OPEN_BRACKET | M_CLOSE_BRACKET | no_math_content
  private static boolean display_math_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "display_math_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, M_OPEN_BRACKET);
    if (!r) r = consumeToken(b, M_CLOSE_BRACKET);
    if (!r) r = no_math_content(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // END_TOKEN STAR? parameter*
  public static boolean end_command(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "end_command")) return false;
    if (!nextTokenIs(b, END_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, END_TOKEN);
    r = r && end_command_1(b, l + 1);
    r = r && end_command_2(b, l + 1);
    exit_section_(b, m, END_COMMAND, r);
    return r;
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
    int c = current_position_(b);
    while (true) {
      if (!parameter(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "end_command_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // begin_command content* end_command
  public static boolean environment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "environment")) return false;
    if (!nextTokenIs(b, BEGIN_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = begin_command(b, l + 1);
    r = r && environment_1(b, l + 1);
    r = r && end_command(b, l + 1);
    exit_section_(b, m, ENVIRONMENT, r);
    return r;
  }

  // content*
  private static boolean environment_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "environment_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "environment_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // OPEN_BRACE content* CLOSE_BRACE
  public static boolean group(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group")) return false;
    if (!nextTokenIs(b, OPEN_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPEN_BRACE);
    r = r && group_1(b, l + 1);
    r = r && consumeToken(b, CLOSE_BRACE);
    exit_section_(b, m, GROUP, r);
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
  // INLINE_MATH_START (M_OPEN_BRACKET | M_CLOSE_BRACKET | no_math_content)* INLINE_MATH_END
  public static boolean inline_math(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math")) return false;
    if (!nextTokenIs(b, INLINE_MATH_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INLINE_MATH_START);
    r = r && inline_math_1(b, l + 1);
    r = r && consumeToken(b, INLINE_MATH_END);
    exit_section_(b, m, INLINE_MATH, r);
    return r;
  }

  // (M_OPEN_BRACKET | M_CLOSE_BRACKET | no_math_content)*
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

  // M_OPEN_BRACKET | M_CLOSE_BRACKET | no_math_content
  private static boolean inline_math_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, M_OPEN_BRACKET);
    if (!r) r = consumeToken(b, M_CLOSE_BRACKET);
    if (!r) r = no_math_content(b, l + 1);
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
    if (!nextTokenIs(b, "<math environment>", DISPLAY_MATH_START, INLINE_MATH_START)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MATH_ENVIRONMENT, "<math environment>");
    r = inline_math(b, l + 1);
    if (!r) r = display_math(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // comment | environment | commands | group | open_group | OPEN_PAREN | CLOSE_PAREN | NORMAL_TEXT
  public static boolean no_math_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "no_math_content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NO_MATH_CONTENT, "<no math content>");
    r = comment(b, l + 1);
    if (!r) r = environment(b, l + 1);
    if (!r) r = commands(b, l + 1);
    if (!r) r = group(b, l + 1);
    if (!r) r = open_group(b, l + 1);
    if (!r) r = consumeToken(b, OPEN_PAREN);
    if (!r) r = consumeToken(b, CLOSE_PAREN);
    if (!r) r = consumeToken(b, NORMAL_TEXT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // OPEN_BRACKET content* CLOSE_BRACKET
  public static boolean open_group(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "open_group")) return false;
    if (!nextTokenIs(b, OPEN_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPEN_BRACKET);
    r = r && open_group_1(b, l + 1);
    r = r && consumeToken(b, CLOSE_BRACKET);
    exit_section_(b, m, OPEN_GROUP, r);
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
