// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static nl.hannahsten.texifyidea.psi.LatexTypes.*;
import static nl.hannahsten.texifyidea.parser.LatexParserUtil.*;
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
  // <<injection_env_content raw_text>> | content+
  public static boolean environment_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "environment_content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENVIRONMENT_CONTENT, "<environment content>");
    r = injection_env_content(b, l + 1, raw_text_parser_);
    if (!r) r = environment_content_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // content+
  private static boolean environment_content_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "environment_content_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = content(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "environment_content_1", c)) break;
    }
    exit_section_(b, m, null, r);
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
  // raw_text | comment | environment | pseudocode_block | math_environment | COMMAND_IFNEXTCHAR | commands | group | OPEN_PAREN | CLOSE_PAREN | M_OPEN_BRACKET | M_CLOSE_BRACKET | OPEN_BRACKET | CLOSE_BRACKET | normal_text
  public static boolean no_math_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "no_math_content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NO_MATH_CONTENT, "<no math content>");
    r = raw_text(b, l + 1);
    if (!r) r = comment(b, l + 1);
    if (!r) r = environment(b, l + 1);
    if (!r) r = pseudocode_block(b, l + 1);
    if (!r) r = math_environment(b, l + 1);
    if (!r) r = consumeToken(b, COMMAND_IFNEXTCHAR);
    if (!r) r = commands(b, l + 1);
    if (!r) r = group(b, l + 1);
    if (!r) r = consumeToken(b, OPEN_PAREN);
    if (!r) r = consumeToken(b, CLOSE_PAREN);
    if (!r) r = consumeToken(b, M_OPEN_BRACKET);
    if (!r) r = consumeToken(b, M_CLOSE_BRACKET);
    if (!r) r = consumeToken(b, OPEN_BRACKET);
    if (!r) r = consumeToken(b, CLOSE_BRACKET);
    if (!r) r = normal_text(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (NORMAL_TEXT_WORD | STAR | AMPERSAND | NORMAL_TEXT_CHAR)+
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

  // NORMAL_TEXT_WORD | STAR | AMPERSAND | NORMAL_TEXT_CHAR
  private static boolean normal_text_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_text_0")) return false;
    boolean r;
    r = consumeToken(b, NORMAL_TEXT_WORD);
    if (!r) r = consumeToken(b, STAR);
    if (!r) r = consumeToken(b, AMPERSAND);
    if (!r) r = consumeToken(b, NORMAL_TEXT_CHAR);
    return r;
  }

  /* ********************************************************** */
  // OPEN_BRACKET optional_param_content* CLOSE_BRACKET
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

  // optional_param_content*
  private static boolean optional_param_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_param_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!optional_param_content(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "optional_param_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // comment | environment | math_environment | commands | group | OPEN_PAREN | CLOSE_PAREN | normal_text
  public static boolean optional_param_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_param_content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OPTIONAL_PARAM_CONTENT, "<optional param content>");
    r = comment(b, l + 1);
    if (!r) r = environment(b, l + 1);
    if (!r) r = math_environment(b, l + 1);
    if (!r) r = commands(b, l + 1);
    if (!r) r = group(b, l + 1);
    if (!r) r = consumeToken(b, OPEN_PAREN);
    if (!r) r = consumeToken(b, CLOSE_PAREN);
    if (!r) r = normal_text(b, l + 1);
    exit_section_(b, l, m, r, false, null);
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
  // BEGIN_PSEUDOCODE_BLOCK parameter* environment_content? END_PSEUDOCODE_BLOCK parameter*
  public static boolean pseudocode_block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pseudocode_block")) return false;
    if (!nextTokenIs(b, BEGIN_PSEUDOCODE_BLOCK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PSEUDOCODE_BLOCK, null);
    r = consumeToken(b, BEGIN_PSEUDOCODE_BLOCK);
    r = r && pseudocode_block_1(b, l + 1);
    r = r && pseudocode_block_2(b, l + 1);
    r = r && consumeToken(b, END_PSEUDOCODE_BLOCK);
    p = r; // pin = 4
    r = r && pseudocode_block_4(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
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

  // environment_content?
  private static boolean pseudocode_block_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pseudocode_block_2")) return false;
    environment_content(b, l + 1);
    return true;
  }

  // parameter*
  private static boolean pseudocode_block_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pseudocode_block_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameter(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "pseudocode_block_4", c)) break;
    }
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

  static final Parser raw_text_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return raw_text(b, l + 1);
    }
  };
}
