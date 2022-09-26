// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static nl.hannahsten.texifyidea.psi.LatexTypes.*;
import static nl.hannahsten.texifyidea.psi.LatexParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import static com.intellij.lang.WhitespacesBinders.*;

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
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return latexFile(b, l + 1);
  }

  /* ********************************************************** */
  // OPEN_ANGLE_BRACKET angle_param_content* CLOSE_ANGLE_BRACKET
  public static boolean angle_param(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "angle_param")) return false;
    if (!nextTokenIs(b, OPEN_ANGLE_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPEN_ANGLE_BRACKET);
    r = r && angle_param_1(b, l + 1);
    r = r && consumeToken(b, CLOSE_ANGLE_BRACKET);
    exit_section_(b, m, ANGLE_PARAM, r);
    return r;
  }

  // angle_param_content*
  private static boolean angle_param_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "angle_param_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!angle_param_content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "angle_param_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // raw_text | magic_comment | comment | environment | pseudocode_block | math_environment | COMMAND_IFNEXTCHAR | commands | parameter_group | parameter_text | BACKSLASH | COMMA | EQUALS | OPEN_BRACKET | CLOSE_BRACKET
  public static boolean angle_param_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "angle_param_content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ANGLE_PARAM_CONTENT, "<angle param content>");
    r = raw_text(b, l + 1);
    if (!r) r = magic_comment(b, l + 1);
    if (!r) r = comment(b, l + 1);
    if (!r) r = environment(b, l + 1);
    if (!r) r = pseudocode_block(b, l + 1);
    if (!r) r = math_environment(b, l + 1);
    if (!r) r = consumeToken(b, COMMAND_IFNEXTCHAR);
    if (!r) r = commands(b, l + 1);
    if (!r) r = parameter_group(b, l + 1);
    if (!r) r = parameter_text(b, l + 1);
    if (!r) r = consumeToken(b, BACKSLASH);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, EQUALS);
    if (!r) r = consumeToken(b, OPEN_BRACKET);
    if (!r) r = consumeToken(b, CLOSE_BRACKET);
    exit_section_(b, l, m, r, false, null);
    return r;
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
  // no_math_content*
  public static boolean content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "content")) return false;
    Marker m = enter_section_(b, l, _NONE_, CONTENT, "<content>");
    while (true) {
      int c = current_position_(b);
      if (!no_math_content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "content", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
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
  // <<injection_env_content raw_text>> | no_math_content+
  public static boolean environment_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "environment_content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENVIRONMENT_CONTENT, "<environment content>");
    r = injection_env_content(b, l + 1, LatexParser::raw_text);
    if (!r) r = environment_content_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // no_math_content+
  private static boolean environment_content_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "environment_content_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = no_math_content(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!no_math_content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "environment_content_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // OPEN_BRACE content CLOSE_BRACE
  public static boolean group(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group")) return false;
    if (!nextTokenIs(b, OPEN_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GROUP, null);
    r = consumeToken(b, OPEN_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, content(b, l + 1));
    r = p && consumeToken(b, CLOSE_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // (group | NORMAL_TEXT_WORD | STAR | AMPERSAND | QUOTATION_MARK | PIPE | EXCLAMATION_MARK | DASH)+
  public static boolean keyval_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyval_key")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYVAL_KEY, "<keyval key>");
    r = keyval_key_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!keyval_key_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyval_key", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // group | NORMAL_TEXT_WORD | STAR | AMPERSAND | QUOTATION_MARK | PIPE | EXCLAMATION_MARK | DASH
  private static boolean keyval_key_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyval_key_0")) return false;
    boolean r;
    r = group(b, l + 1);
    if (!r) r = consumeToken(b, NORMAL_TEXT_WORD);
    if (!r) r = consumeToken(b, STAR);
    if (!r) r = consumeToken(b, AMPERSAND);
    if (!r) r = consumeToken(b, QUOTATION_MARK);
    if (!r) r = consumeToken(b, PIPE);
    if (!r) r = consumeToken(b, EXCLAMATION_MARK);
    if (!r) r = consumeToken(b, DASH);
    return r;
  }

  /* ********************************************************** */
  // keyval_key (EQUALS keyval_value?)?
  public static boolean keyval_pair(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyval_pair")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYVAL_PAIR, "<keyval pair>");
    r = keyval_key(b, l + 1);
    r = r && keyval_pair_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (EQUALS keyval_value?)?
  private static boolean keyval_pair_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyval_pair_1")) return false;
    keyval_pair_1_0(b, l + 1);
    return true;
  }

  // EQUALS keyval_value?
  private static boolean keyval_pair_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyval_pair_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EQUALS);
    r = r && keyval_pair_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // keyval_value?
  private static boolean keyval_pair_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyval_pair_1_0_1")) return false;
    keyval_value(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // optional_param_content+
  public static boolean keyval_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyval_value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYVAL_VALUE, "<keyval value>");
    r = optional_param_content(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!optional_param_content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyval_value", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // content
  static boolean latexFile(PsiBuilder b, int l) {
    return content(b, l + 1);
  }

  /* ********************************************************** */
  // MAGIC_COMMENT_TOKEN
  public static boolean magic_comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "magic_comment")) return false;
    if (!nextTokenIs(b, MAGIC_COMMENT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MAGIC_COMMENT_TOKEN);
    exit_section_(b, m, MAGIC_COMMENT, r);
    return r;
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
  // raw_text | magic_comment | comment | environment | pseudocode_block | math_environment | COMMAND_IFNEXTCHAR | commands | group |
  //     OPEN_PAREN | CLOSE_PAREN | OPEN_BRACKET | CLOSE_BRACKET | normal_text
  public static boolean no_math_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "no_math_content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NO_MATH_CONTENT, "<no math content>");
    r = raw_text(b, l + 1);
    if (!r) r = magic_comment(b, l + 1);
    if (!r) r = comment(b, l + 1);
    if (!r) r = environment(b, l + 1);
    if (!r) r = pseudocode_block(b, l + 1);
    if (!r) r = math_environment(b, l + 1);
    if (!r) r = consumeToken(b, COMMAND_IFNEXTCHAR);
    if (!r) r = commands(b, l + 1);
    if (!r) r = group(b, l + 1);
    if (!r) r = consumeToken(b, OPEN_PAREN);
    if (!r) r = consumeToken(b, CLOSE_PAREN);
    if (!r) r = consumeToken(b, OPEN_BRACKET);
    if (!r) r = consumeToken(b, CLOSE_BRACKET);
    if (!r) r = normal_text(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (NORMAL_TEXT_WORD | STAR | AMPERSAND | QUOTATION_MARK | OPEN_ANGLE_BRACKET | CLOSE_ANGLE_BRACKET | PIPE | EXCLAMATION_MARK | BACKSLASH | EQUALS | COMMA | DASH)+
  public static boolean normal_text(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_text")) return false;
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

  // NORMAL_TEXT_WORD | STAR | AMPERSAND | QUOTATION_MARK | OPEN_ANGLE_BRACKET | CLOSE_ANGLE_BRACKET | PIPE | EXCLAMATION_MARK | BACKSLASH | EQUALS | COMMA | DASH
  private static boolean normal_text_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_text_0")) return false;
    boolean r;
    r = consumeToken(b, NORMAL_TEXT_WORD);
    if (!r) r = consumeToken(b, STAR);
    if (!r) r = consumeToken(b, AMPERSAND);
    if (!r) r = consumeToken(b, QUOTATION_MARK);
    if (!r) r = consumeToken(b, OPEN_ANGLE_BRACKET);
    if (!r) r = consumeToken(b, CLOSE_ANGLE_BRACKET);
    if (!r) r = consumeToken(b, PIPE);
    if (!r) r = consumeToken(b, EXCLAMATION_MARK);
    if (!r) r = consumeToken(b, BACKSLASH);
    if (!r) r = consumeToken(b, EQUALS);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, DASH);
    return r;
  }

  /* ********************************************************** */
  // OPEN_BRACKET ( (keyval_pair  (COMMA keyval_pair)* COMMA?) | optional_param_content*) CLOSE_BRACKET
  public static boolean optional_param(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_param")) return false;
    if (!nextTokenIs(b, OPEN_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPEN_BRACKET);
    r = r && optional_param_1(b, l + 1);
    r = r && consumeToken(b, CLOSE_BRACKET);
    exit_section_(b, m, OPTIONAL_PARAM, r);
    return r;
  }

  // (keyval_pair  (COMMA keyval_pair)* COMMA?) | optional_param_content*
  private static boolean optional_param_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_param_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = optional_param_1_0(b, l + 1);
    if (!r) r = optional_param_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // keyval_pair  (COMMA keyval_pair)* COMMA?
  private static boolean optional_param_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_param_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyval_pair(b, l + 1);
    r = r && optional_param_1_0_1(b, l + 1);
    r = r && optional_param_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA keyval_pair)*
  private static boolean optional_param_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_param_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!optional_param_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "optional_param_1_0_1", c)) break;
    }
    return true;
  }

  // COMMA keyval_pair
  private static boolean optional_param_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_param_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && keyval_pair(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean optional_param_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_param_1_0_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  // optional_param_content*
  private static boolean optional_param_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_param_1_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!optional_param_content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "optional_param_1_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // raw_text | magic_comment | comment | environment | pseudocode_block | math_environment | COMMAND_IFNEXTCHAR | commands | parameter_group | OPEN_PAREN | CLOSE_PAREN | parameter_text | BACKSLASH | OPEN_ANGLE_BRACKET | CLOSE_ANGLE_BRACKET
  public static boolean optional_param_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_param_content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OPTIONAL_PARAM_CONTENT, "<optional param content>");
    r = raw_text(b, l + 1);
    if (!r) r = magic_comment(b, l + 1);
    if (!r) r = comment(b, l + 1);
    if (!r) r = environment(b, l + 1);
    if (!r) r = pseudocode_block(b, l + 1);
    if (!r) r = math_environment(b, l + 1);
    if (!r) r = consumeToken(b, COMMAND_IFNEXTCHAR);
    if (!r) r = commands(b, l + 1);
    if (!r) r = parameter_group(b, l + 1);
    if (!r) r = consumeToken(b, OPEN_PAREN);
    if (!r) r = consumeToken(b, CLOSE_PAREN);
    if (!r) r = parameter_text(b, l + 1);
    if (!r) r = consumeToken(b, BACKSLASH);
    if (!r) r = consumeToken(b, OPEN_ANGLE_BRACKET);
    if (!r) r = consumeToken(b, CLOSE_ANGLE_BRACKET);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // optional_param | required_param | picture_param | angle_param
  public static boolean parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER, "<parameter>");
    r = optional_param(b, l + 1);
    if (!r) r = required_param(b, l + 1);
    if (!r) r = picture_param(b, l + 1);
    if (!r) r = angle_param(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // OPEN_BRACE parameter_group_text CLOSE_BRACE
  public static boolean parameter_group(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_group")) return false;
    if (!nextTokenIs(b, OPEN_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_GROUP, null);
    r = consumeToken(b, OPEN_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, parameter_group_text(b, l + 1));
    r = p && consumeToken(b, CLOSE_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (parameter_text | group | COMMA | EQUALS | OPEN_BRACKET | CLOSE_BRACKET)*
  public static boolean parameter_group_text(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_group_text")) return false;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_GROUP_TEXT, "<parameter group text>");
    while (true) {
      int c = current_position_(b);
      if (!parameter_group_text_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameter_group_text", c)) break;
    }
    register_hook_(b, WS_BINDERS, GREEDY_LEFT_BINDER, GREEDY_RIGHT_BINDER);
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  // parameter_text | group | COMMA | EQUALS | OPEN_BRACKET | CLOSE_BRACKET
  private static boolean parameter_group_text_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_group_text_0")) return false;
    boolean r;
    r = parameter_text(b, l + 1);
    if (!r) r = group(b, l + 1);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, EQUALS);
    if (!r) r = consumeToken(b, OPEN_BRACKET);
    if (!r) r = consumeToken(b, CLOSE_BRACKET);
    return r;
  }

  /* ********************************************************** */
  // (commands | NORMAL_TEXT_WORD | STAR | AMPERSAND | QUOTATION_MARK | PIPE | EXCLAMATION_MARK | DASH)+
  public static boolean parameter_text(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_text")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_TEXT, "<parameter text>");
    r = parameter_text_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!parameter_text_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameter_text", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // commands | NORMAL_TEXT_WORD | STAR | AMPERSAND | QUOTATION_MARK | PIPE | EXCLAMATION_MARK | DASH
  private static boolean parameter_text_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_text_0")) return false;
    boolean r;
    r = commands(b, l + 1);
    if (!r) r = consumeToken(b, NORMAL_TEXT_WORD);
    if (!r) r = consumeToken(b, STAR);
    if (!r) r = consumeToken(b, AMPERSAND);
    if (!r) r = consumeToken(b, QUOTATION_MARK);
    if (!r) r = consumeToken(b, PIPE);
    if (!r) r = consumeToken(b, EXCLAMATION_MARK);
    if (!r) r = consumeToken(b, DASH);
    return r;
  }

  /* ********************************************************** */
  // OPEN_PAREN picture_param_content* CLOSE_PAREN
  public static boolean picture_param(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "picture_param")) return false;
    if (!nextTokenIs(b, OPEN_PAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPEN_PAREN);
    r = r && picture_param_1(b, l + 1);
    r = r && consumeToken(b, CLOSE_PAREN);
    exit_section_(b, m, PICTURE_PARAM, r);
    return r;
  }

  // picture_param_content*
  private static boolean picture_param_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "picture_param_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!picture_param_content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "picture_param_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // raw_text | magic_comment | comment | environment | pseudocode_block | math_environment | COMMAND_IFNEXTCHAR | commands | parameter_group | parameter_text | BACKSLASH | COMMA | EQUALS | OPEN_BRACKET | CLOSE_BRACKET | OPEN_ANGLE_BRACKET | CLOSE_ANGLE_BRACKET
  public static boolean picture_param_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "picture_param_content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PICTURE_PARAM_CONTENT, "<picture param content>");
    r = raw_text(b, l + 1);
    if (!r) r = magic_comment(b, l + 1);
    if (!r) r = comment(b, l + 1);
    if (!r) r = environment(b, l + 1);
    if (!r) r = pseudocode_block(b, l + 1);
    if (!r) r = math_environment(b, l + 1);
    if (!r) r = consumeToken(b, COMMAND_IFNEXTCHAR);
    if (!r) r = commands(b, l + 1);
    if (!r) r = parameter_group(b, l + 1);
    if (!r) r = parameter_text(b, l + 1);
    if (!r) r = consumeToken(b, BACKSLASH);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, EQUALS);
    if (!r) r = consumeToken(b, OPEN_BRACKET);
    if (!r) r = consumeToken(b, CLOSE_BRACKET);
    if (!r) r = consumeToken(b, OPEN_ANGLE_BRACKET);
    if (!r) r = consumeToken(b, CLOSE_ANGLE_BRACKET);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // BEGIN_PSEUDOCODE_BLOCK parameter* pseudocode_block_content? (MIDDLE_PSEUDOCODE_BLOCK pseudocode_block_content?)* (END_PSEUDOCODE_BLOCK parameter*)
  public static boolean pseudocode_block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pseudocode_block")) return false;
    if (!nextTokenIs(b, BEGIN_PSEUDOCODE_BLOCK)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, BEGIN_PSEUDOCODE_BLOCK);
    r = r && pseudocode_block_1(b, l + 1);
    r = r && pseudocode_block_2(b, l + 1);
    r = r && pseudocode_block_3(b, l + 1);
    r = r && pseudocode_block_4(b, l + 1);
    exit_section_(b, m, PSEUDOCODE_BLOCK, r);
    return r;
  }

  // parameter*
  private static boolean pseudocode_block_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pseudocode_block_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameter(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "pseudocode_block_1", c)) break;
    }
    return true;
  }

  // pseudocode_block_content?
  private static boolean pseudocode_block_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pseudocode_block_2")) return false;
    pseudocode_block_content(b, l + 1);
    return true;
  }

  // (MIDDLE_PSEUDOCODE_BLOCK pseudocode_block_content?)*
  private static boolean pseudocode_block_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pseudocode_block_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!pseudocode_block_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "pseudocode_block_3", c)) break;
    }
    return true;
  }

  // MIDDLE_PSEUDOCODE_BLOCK pseudocode_block_content?
  private static boolean pseudocode_block_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pseudocode_block_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MIDDLE_PSEUDOCODE_BLOCK);
    r = r && pseudocode_block_3_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // pseudocode_block_content?
  private static boolean pseudocode_block_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pseudocode_block_3_0_1")) return false;
    pseudocode_block_content(b, l + 1);
    return true;
  }

  // END_PSEUDOCODE_BLOCK parameter*
  private static boolean pseudocode_block_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pseudocode_block_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, END_PSEUDOCODE_BLOCK);
    r = r && pseudocode_block_4_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // parameter*
  private static boolean pseudocode_block_4_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pseudocode_block_4_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameter(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "pseudocode_block_4_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // no_math_content*
  public static boolean pseudocode_block_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pseudocode_block_content")) return false;
    Marker m = enter_section_(b, l, _NONE_, PSEUDOCODE_BLOCK_CONTENT, "<pseudocode block content>");
    while (true) {
      int c = current_position_(b);
      if (!no_math_content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "pseudocode_block_content", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // RAW_TEXT_TOKEN+
  public static boolean raw_text(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "raw_text")) return false;
    if (!nextTokenIs(b, RAW_TEXT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RAW_TEXT_TOKEN);
    while (r) {
      int c = current_position_(b);
      if (!consumeToken(b, RAW_TEXT_TOKEN)) break;
      if (!empty_element_parsed_guard_(b, "raw_text", c)) break;
    }
    exit_section_(b, m, RAW_TEXT, r);
    return r;
  }

  /* ********************************************************** */
  // OPEN_BRACE (strict_keyval_pair (COMMA strict_keyval_pair)* CLOSE_BRACE | required_param_content* CLOSE_BRACE)
  public static boolean required_param(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_param")) return false;
    if (!nextTokenIs(b, OPEN_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, REQUIRED_PARAM, null);
    r = consumeToken(b, OPEN_BRACE);
    p = r; // pin = 1
    r = r && required_param_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // strict_keyval_pair (COMMA strict_keyval_pair)* CLOSE_BRACE | required_param_content* CLOSE_BRACE
  private static boolean required_param_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_param_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = required_param_1_0(b, l + 1);
    if (!r) r = required_param_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // strict_keyval_pair (COMMA strict_keyval_pair)* CLOSE_BRACE
  private static boolean required_param_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_param_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = strict_keyval_pair(b, l + 1);
    r = r && required_param_1_0_1(b, l + 1);
    r = r && consumeToken(b, CLOSE_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA strict_keyval_pair)*
  private static boolean required_param_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_param_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!required_param_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "required_param_1_0_1", c)) break;
    }
    return true;
  }

  // COMMA strict_keyval_pair
  private static boolean required_param_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_param_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && strict_keyval_pair(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // required_param_content* CLOSE_BRACE
  private static boolean required_param_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_param_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = required_param_1_1_0(b, l + 1);
    r = r && consumeToken(b, CLOSE_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // required_param_content*
  private static boolean required_param_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_param_1_1_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!required_param_content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "required_param_1_1_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // raw_text | magic_comment | comment | environment | pseudocode_block | math_environment | COMMAND_IFNEXTCHAR | parameter_group | OPEN_PAREN | CLOSE_PAREN | parameter_text | COMMA | EQUALS | OPEN_BRACKET | CLOSE_BRACKET | BACKSLASH | OPEN_ANGLE_BRACKET | CLOSE_ANGLE_BRACKET
  public static boolean required_param_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_param_content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REQUIRED_PARAM_CONTENT, "<required param content>");
    r = raw_text(b, l + 1);
    if (!r) r = magic_comment(b, l + 1);
    if (!r) r = comment(b, l + 1);
    if (!r) r = environment(b, l + 1);
    if (!r) r = pseudocode_block(b, l + 1);
    if (!r) r = math_environment(b, l + 1);
    if (!r) r = consumeToken(b, COMMAND_IFNEXTCHAR);
    if (!r) r = parameter_group(b, l + 1);
    if (!r) r = consumeToken(b, OPEN_PAREN);
    if (!r) r = consumeToken(b, CLOSE_PAREN);
    if (!r) r = parameter_text(b, l + 1);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, EQUALS);
    if (!r) r = consumeToken(b, OPEN_BRACKET);
    if (!r) r = consumeToken(b, CLOSE_BRACKET);
    if (!r) r = consumeToken(b, BACKSLASH);
    if (!r) r = consumeToken(b, OPEN_ANGLE_BRACKET);
    if (!r) r = consumeToken(b, CLOSE_ANGLE_BRACKET);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // keyval_key EQUALS keyval_value?
  public static boolean strict_keyval_pair(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "strict_keyval_pair")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRICT_KEYVAL_PAIR, "<strict keyval pair>");
    r = keyval_key(b, l + 1);
    r = r && consumeToken(b, EQUALS);
    r = r && strict_keyval_pair_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // keyval_value?
  private static boolean strict_keyval_pair_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "strict_keyval_pair_2")) return false;
    keyval_value(b, l + 1);
    return true;
  }

}
