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

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, null);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    result_ = parse_root_(root_, builder_);
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType root_, PsiBuilder builder_) {
    return parse_root_(root_, builder_, 0);
  }

  static boolean parse_root_(IElementType root_, PsiBuilder builder_, int level_) {
    return latexFile(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // BEGIN_TOKEN STAR? parameter*
  public static boolean begin_command(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "begin_command")) return false;
    if (!nextTokenIs(builder_, BEGIN_TOKEN)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BEGIN_COMMAND, null);
    result_ = consumeToken(builder_, BEGIN_TOKEN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, begin_command_1(builder_, level_ + 1));
    result_ = pinned_ && begin_command_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // STAR?
  private static boolean begin_command_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "begin_command_1")) return false;
    consumeToken(builder_, STAR);
    return true;
  }

  // parameter*
  private static boolean begin_command_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "begin_command_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!parameter(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "begin_command_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // COMMAND_TOKEN STAR? parameter*
  public static boolean commands(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "commands")) return false;
    if (!nextTokenIs(builder_, COMMAND_TOKEN)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COMMANDS, null);
    result_ = consumeToken(builder_, COMMAND_TOKEN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, commands_1(builder_, level_ + 1));
    result_ = pinned_ && commands_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // STAR?
  private static boolean commands_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "commands_1")) return false;
    consumeToken(builder_, STAR);
    return true;
  }

  // parameter*
  private static boolean commands_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "commands_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!parameter(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "commands_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // COMMENT_TOKEN
  public static boolean comment(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comment")) return false;
    if (!nextTokenIs(builder_, COMMENT_TOKEN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMENT_TOKEN);
    exit_section_(builder_, marker_, COMMENT, result_);
    return result_;
  }

  /* ********************************************************** */
  // no_math_content*
  public static boolean content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "content")) return false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONTENT, "<content>");
    while (true) {
      int pos_ = current_position_(builder_);
      if (!no_math_content(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "content", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // DISPLAY_MATH_START math_content? DISPLAY_MATH_END
  public static boolean display_math(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "display_math")) return false;
    if (!nextTokenIs(builder_, DISPLAY_MATH_START)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DISPLAY_MATH, null);
    result_ = consumeToken(builder_, DISPLAY_MATH_START);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, display_math_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, DISPLAY_MATH_END) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // math_content?
  private static boolean display_math_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "display_math_1")) return false;
    math_content(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // END_TOKEN STAR? parameter*
  public static boolean end_command(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "end_command")) return false;
    if (!nextTokenIs(builder_, END_TOKEN)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, END_COMMAND, null);
    result_ = consumeToken(builder_, END_TOKEN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, end_command_1(builder_, level_ + 1));
    result_ = pinned_ && end_command_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // STAR?
  private static boolean end_command_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "end_command_1")) return false;
    consumeToken(builder_, STAR);
    return true;
  }

  // parameter*
  private static boolean end_command_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "end_command_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!parameter(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "end_command_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // begin_command environment_content? end_command
  public static boolean environment(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "environment")) return false;
    if (!nextTokenIs(builder_, BEGIN_TOKEN)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ENVIRONMENT, null);
    result_ = begin_command(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, environment_1(builder_, level_ + 1));
    result_ = pinned_ && end_command(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // environment_content?
  private static boolean environment_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "environment_1")) return false;
    environment_content(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // <<injection_env_content raw_text>> | no_math_content+
  public static boolean environment_content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "environment_content")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ENVIRONMENT_CONTENT, "<environment content>");
    result_ = injection_env_content(builder_, level_ + 1, LatexParser::raw_text);
    if (!result_) result_ = environment_content_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // no_math_content+
  private static boolean environment_content_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "environment_content_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = no_math_content(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!no_math_content(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "environment_content_1", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // OPEN_BRACE content CLOSE_BRACE
  public static boolean group(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "group")) return false;
    if (!nextTokenIs(builder_, OPEN_BRACE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, GROUP, null);
    result_ = consumeToken(builder_, OPEN_BRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, content(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, CLOSE_BRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // INLINE_MATH_START math_content? INLINE_MATH_END
  public static boolean inline_math(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "inline_math")) return false;
    if (!nextTokenIs(builder_, INLINE_MATH_START)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INLINE_MATH, null);
    result_ = consumeToken(builder_, INLINE_MATH_START);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, inline_math_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, INLINE_MATH_END) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // math_content?
  private static boolean inline_math_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "inline_math_1")) return false;
    math_content(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // parameter_text | parameter_group | OPEN_PAREN | CLOSE_PAREN | OPEN_ANGLE_BRACKET | CLOSE_ANGLE_BRACKET | commands | math_environment
  public static boolean key_val_content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key_val_content")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, KEY_VAL_CONTENT, "<key val content>");
    result_ = parameter_text(builder_, level_ + 1);
    if (!result_) result_ = parameter_group(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, OPEN_PAREN);
    if (!result_) result_ = consumeToken(builder_, CLOSE_PAREN);
    if (!result_) result_ = consumeToken(builder_, OPEN_ANGLE_BRACKET);
    if (!result_) result_ = consumeToken(builder_, CLOSE_ANGLE_BRACKET);
    if (!result_) result_ = commands(builder_, level_ + 1);
    if (!result_) result_ = math_environment(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // (group | NORMAL_TEXT_WORD | STAR | AMPERSAND | QUOTATION_MARK | PIPE | EXCLAMATION_MARK)+
  public static boolean key_val_key(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key_val_key")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, KEY_VAL_KEY, "<key val key>");
    result_ = key_val_key_0(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!key_val_key_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "key_val_key", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // group | NORMAL_TEXT_WORD | STAR | AMPERSAND | QUOTATION_MARK | PIPE | EXCLAMATION_MARK
  private static boolean key_val_key_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key_val_key_0")) return false;
    boolean result_;
    result_ = group(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, NORMAL_TEXT_WORD);
    if (!result_) result_ = consumeToken(builder_, STAR);
    if (!result_) result_ = consumeToken(builder_, AMPERSAND);
    if (!result_) result_ = consumeToken(builder_, QUOTATION_MARK);
    if (!result_) result_ = consumeToken(builder_, PIPE);
    if (!result_) result_ = consumeToken(builder_, EXCLAMATION_MARK);
    return result_;
  }

  /* ********************************************************** */
  // key_val_key (EQUALS key_val_value?)?
  public static boolean key_val_pair(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key_val_pair")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, KEY_VAL_PAIR, "<key val pair>");
    result_ = key_val_key(builder_, level_ + 1);
    result_ = result_ && key_val_pair_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (EQUALS key_val_value?)?
  private static boolean key_val_pair_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key_val_pair_1")) return false;
    key_val_pair_1_0(builder_, level_ + 1);
    return true;
  }

  // EQUALS key_val_value?
  private static boolean key_val_pair_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key_val_pair_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EQUALS);
    result_ = result_ && key_val_pair_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // key_val_value?
  private static boolean key_val_pair_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key_val_pair_1_0_1")) return false;
    key_val_value(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // key_val_content+
  public static boolean key_val_value(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key_val_value")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, KEY_VAL_VALUE, "<key val value>");
    result_ = key_val_content(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!key_val_content(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "key_val_value", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // content
  static boolean latexFile(PsiBuilder builder_, int level_) {
    return content(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // MAGIC_COMMENT_TOKEN
  public static boolean magic_comment(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "magic_comment")) return false;
    if (!nextTokenIs(builder_, MAGIC_COMMENT_TOKEN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MAGIC_COMMENT_TOKEN);
    exit_section_(builder_, marker_, MAGIC_COMMENT, result_);
    return result_;
  }

  /* ********************************************************** */
  // no_math_content+
  public static boolean math_content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "math_content")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MATH_CONTENT, "<math content>");
    result_ = no_math_content(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!no_math_content(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "math_content", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // inline_math | display_math
  public static boolean math_environment(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "math_environment")) return false;
    if (!nextTokenIs(builder_, "<math environment>", DISPLAY_MATH_START, INLINE_MATH_START)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MATH_ENVIRONMENT, "<math environment>");
    result_ = inline_math(builder_, level_ + 1);
    if (!result_) result_ = display_math(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // raw_text | magic_comment | comment | environment | pseudocode_block | math_environment | COMMAND_IFNEXTCHAR | commands | group | normal_text
  public static boolean no_math_content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "no_math_content")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, NO_MATH_CONTENT, "<no math content>");
    result_ = raw_text(builder_, level_ + 1);
    if (!result_) result_ = magic_comment(builder_, level_ + 1);
    if (!result_) result_ = comment(builder_, level_ + 1);
    if (!result_) result_ = environment(builder_, level_ + 1);
    if (!result_) result_ = pseudocode_block(builder_, level_ + 1);
    if (!result_) result_ = math_environment(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, COMMAND_IFNEXTCHAR);
    if (!result_) result_ = commands(builder_, level_ + 1);
    if (!result_) result_ = group(builder_, level_ + 1);
    if (!result_) result_ = normal_text(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // (NORMAL_TEXT_WORD | STAR | AMPERSAND | QUOTATION_MARK | OPEN_ANGLE_BRACKET | CLOSE_ANGLE_BRACKET | OPEN_PAREN | CLOSE_PAREN | OPEN_BRACKET | CLOSE_BRACKET | PIPE | EXCLAMATION_MARK | BACKSLASH | EQUALS | COMMA | ANGLE_PARAM)+
  public static boolean normal_text(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "normal_text")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, NORMAL_TEXT, "<normal text>");
    result_ = normal_text_0(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!normal_text_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "normal_text", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // NORMAL_TEXT_WORD | STAR | AMPERSAND | QUOTATION_MARK | OPEN_ANGLE_BRACKET | CLOSE_ANGLE_BRACKET | OPEN_PAREN | CLOSE_PAREN | OPEN_BRACKET | CLOSE_BRACKET | PIPE | EXCLAMATION_MARK | BACKSLASH | EQUALS | COMMA | ANGLE_PARAM
  private static boolean normal_text_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "normal_text_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, NORMAL_TEXT_WORD);
    if (!result_) result_ = consumeToken(builder_, STAR);
    if (!result_) result_ = consumeToken(builder_, AMPERSAND);
    if (!result_) result_ = consumeToken(builder_, QUOTATION_MARK);
    if (!result_) result_ = consumeToken(builder_, OPEN_ANGLE_BRACKET);
    if (!result_) result_ = consumeToken(builder_, CLOSE_ANGLE_BRACKET);
    if (!result_) result_ = consumeToken(builder_, OPEN_PAREN);
    if (!result_) result_ = consumeToken(builder_, CLOSE_PAREN);
    if (!result_) result_ = consumeToken(builder_, OPEN_BRACKET);
    if (!result_) result_ = consumeToken(builder_, CLOSE_BRACKET);
    if (!result_) result_ = consumeToken(builder_, PIPE);
    if (!result_) result_ = consumeToken(builder_, EXCLAMATION_MARK);
    if (!result_) result_ = consumeToken(builder_, BACKSLASH);
    if (!result_) result_ = consumeToken(builder_, EQUALS);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = consumeToken(builder_, ANGLE_PARAM);
    return result_;
  }

  /* ********************************************************** */
  // OPEN_BRACKET ( (key_val_pair  (COMMA key_val_pair)* COMMA?) | optional_param_content*) CLOSE_BRACKET
  public static boolean optional_param(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "optional_param")) return false;
    if (!nextTokenIs(builder_, OPEN_BRACKET)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OPEN_BRACKET);
    result_ = result_ && optional_param_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, CLOSE_BRACKET);
    exit_section_(builder_, marker_, OPTIONAL_PARAM, result_);
    return result_;
  }

  // (key_val_pair  (COMMA key_val_pair)* COMMA?) | optional_param_content*
  private static boolean optional_param_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "optional_param_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = optional_param_1_0(builder_, level_ + 1);
    if (!result_) result_ = optional_param_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // key_val_pair  (COMMA key_val_pair)* COMMA?
  private static boolean optional_param_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "optional_param_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = key_val_pair(builder_, level_ + 1);
    result_ = result_ && optional_param_1_0_1(builder_, level_ + 1);
    result_ = result_ && optional_param_1_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA key_val_pair)*
  private static boolean optional_param_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "optional_param_1_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!optional_param_1_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "optional_param_1_0_1", pos_)) break;
    }
    return true;
  }

  // COMMA key_val_pair
  private static boolean optional_param_1_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "optional_param_1_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && key_val_pair(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // COMMA?
  private static boolean optional_param_1_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "optional_param_1_0_2")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  // optional_param_content*
  private static boolean optional_param_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "optional_param_1_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!optional_param_content(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "optional_param_1_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // raw_text | magic_comment | comment | environment | pseudocode_block | math_environment | COMMAND_IFNEXTCHAR | commands | group | OPEN_PAREN | CLOSE_PAREN | parameter_text | BACKSLASH | OPEN_ANGLE_BRACKET | CLOSE_ANGLE_BRACKET
  public static boolean optional_param_content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "optional_param_content")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, OPTIONAL_PARAM_CONTENT, "<optional param content>");
    result_ = raw_text(builder_, level_ + 1);
    if (!result_) result_ = magic_comment(builder_, level_ + 1);
    if (!result_) result_ = comment(builder_, level_ + 1);
    if (!result_) result_ = environment(builder_, level_ + 1);
    if (!result_) result_ = pseudocode_block(builder_, level_ + 1);
    if (!result_) result_ = math_environment(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, COMMAND_IFNEXTCHAR);
    if (!result_) result_ = commands(builder_, level_ + 1);
    if (!result_) result_ = group(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, OPEN_PAREN);
    if (!result_) result_ = consumeToken(builder_, CLOSE_PAREN);
    if (!result_) result_ = parameter_text(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, BACKSLASH);
    if (!result_) result_ = consumeToken(builder_, OPEN_ANGLE_BRACKET);
    if (!result_) result_ = consumeToken(builder_, CLOSE_ANGLE_BRACKET);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // optional_param | required_param | picture_param | ANGLE_PARAM
  public static boolean parameter(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAMETER, "<parameter>");
    result_ = optional_param(builder_, level_ + 1);
    if (!result_) result_ = required_param(builder_, level_ + 1);
    if (!result_) result_ = picture_param(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, ANGLE_PARAM);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // OPEN_BRACE parameter_group_text CLOSE_BRACE
  public static boolean parameter_group(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_group")) return false;
    if (!nextTokenIs(builder_, OPEN_BRACE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAMETER_GROUP, null);
    result_ = consumeToken(builder_, OPEN_BRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, parameter_group_text(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, CLOSE_BRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // (parameter_text | group | COMMA | EQUALS | OPEN_BRACKET | CLOSE_BRACKET)*
  public static boolean parameter_group_text(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_group_text")) return false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAMETER_GROUP_TEXT, "<parameter group text>");
    while (true) {
      int pos_ = current_position_(builder_);
      if (!parameter_group_text_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "parameter_group_text", pos_)) break;
    }
    register_hook_(builder_, WS_BINDERS, GREEDY_LEFT_BINDER, GREEDY_RIGHT_BINDER);
    exit_section_(builder_, level_, marker_, true, false, null);
    return true;
  }

  // parameter_text | group | COMMA | EQUALS | OPEN_BRACKET | CLOSE_BRACKET
  private static boolean parameter_group_text_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_group_text_0")) return false;
    boolean result_;
    result_ = parameter_text(builder_, level_ + 1);
    if (!result_) result_ = group(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = consumeToken(builder_, EQUALS);
    if (!result_) result_ = consumeToken(builder_, OPEN_BRACKET);
    if (!result_) result_ = consumeToken(builder_, CLOSE_BRACKET);
    return result_;
  }

  /* ********************************************************** */
  // (commands | NORMAL_TEXT_WORD | STAR | AMPERSAND | QUOTATION_MARK | PIPE | EXCLAMATION_MARK)+
  public static boolean parameter_text(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_text")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAMETER_TEXT, "<parameter text>");
    result_ = parameter_text_0(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!parameter_text_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "parameter_text", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // commands | NORMAL_TEXT_WORD | STAR | AMPERSAND | QUOTATION_MARK | PIPE | EXCLAMATION_MARK
  private static boolean parameter_text_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_text_0")) return false;
    boolean result_;
    result_ = commands(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, NORMAL_TEXT_WORD);
    if (!result_) result_ = consumeToken(builder_, STAR);
    if (!result_) result_ = consumeToken(builder_, AMPERSAND);
    if (!result_) result_ = consumeToken(builder_, QUOTATION_MARK);
    if (!result_) result_ = consumeToken(builder_, PIPE);
    if (!result_) result_ = consumeToken(builder_, EXCLAMATION_MARK);
    return result_;
  }

  /* ********************************************************** */
  // OPEN_PAREN picture_param_content* CLOSE_PAREN
  public static boolean picture_param(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "picture_param")) return false;
    if (!nextTokenIs(builder_, OPEN_PAREN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OPEN_PAREN);
    result_ = result_ && picture_param_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, CLOSE_PAREN);
    exit_section_(builder_, marker_, PICTURE_PARAM, result_);
    return result_;
  }

  // picture_param_content*
  private static boolean picture_param_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "picture_param_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!picture_param_content(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "picture_param_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // raw_text | magic_comment | comment | environment | pseudocode_block | math_environment | COMMAND_IFNEXTCHAR | commands | group | parameter_text | BACKSLASH | COMMA | EQUALS | OPEN_BRACKET | CLOSE_BRACKET | OPEN_ANGLE_BRACKET | CLOSE_ANGLE_BRACKET
  public static boolean picture_param_content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "picture_param_content")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PICTURE_PARAM_CONTENT, "<picture param content>");
    result_ = raw_text(builder_, level_ + 1);
    if (!result_) result_ = magic_comment(builder_, level_ + 1);
    if (!result_) result_ = comment(builder_, level_ + 1);
    if (!result_) result_ = environment(builder_, level_ + 1);
    if (!result_) result_ = pseudocode_block(builder_, level_ + 1);
    if (!result_) result_ = math_environment(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, COMMAND_IFNEXTCHAR);
    if (!result_) result_ = commands(builder_, level_ + 1);
    if (!result_) result_ = group(builder_, level_ + 1);
    if (!result_) result_ = parameter_text(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, BACKSLASH);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = consumeToken(builder_, EQUALS);
    if (!result_) result_ = consumeToken(builder_, OPEN_BRACKET);
    if (!result_) result_ = consumeToken(builder_, CLOSE_BRACKET);
    if (!result_) result_ = consumeToken(builder_, OPEN_ANGLE_BRACKET);
    if (!result_) result_ = consumeToken(builder_, CLOSE_ANGLE_BRACKET);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // BEGIN_PSEUDOCODE_BLOCK parameter* pseudocode_block_content? (MIDDLE_PSEUDOCODE_BLOCK pseudocode_block_content?)* (END_PSEUDOCODE_BLOCK parameter*)
  public static boolean pseudocode_block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pseudocode_block")) return false;
    if (!nextTokenIs(builder_, BEGIN_PSEUDOCODE_BLOCK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, BEGIN_PSEUDOCODE_BLOCK);
    result_ = result_ && pseudocode_block_1(builder_, level_ + 1);
    result_ = result_ && pseudocode_block_2(builder_, level_ + 1);
    result_ = result_ && pseudocode_block_3(builder_, level_ + 1);
    result_ = result_ && pseudocode_block_4(builder_, level_ + 1);
    exit_section_(builder_, marker_, PSEUDOCODE_BLOCK, result_);
    return result_;
  }

  // parameter*
  private static boolean pseudocode_block_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pseudocode_block_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!parameter(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "pseudocode_block_1", pos_)) break;
    }
    return true;
  }

  // pseudocode_block_content?
  private static boolean pseudocode_block_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pseudocode_block_2")) return false;
    pseudocode_block_content(builder_, level_ + 1);
    return true;
  }

  // (MIDDLE_PSEUDOCODE_BLOCK pseudocode_block_content?)*
  private static boolean pseudocode_block_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pseudocode_block_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!pseudocode_block_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "pseudocode_block_3", pos_)) break;
    }
    return true;
  }

  // MIDDLE_PSEUDOCODE_BLOCK pseudocode_block_content?
  private static boolean pseudocode_block_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pseudocode_block_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MIDDLE_PSEUDOCODE_BLOCK);
    result_ = result_ && pseudocode_block_3_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // pseudocode_block_content?
  private static boolean pseudocode_block_3_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pseudocode_block_3_0_1")) return false;
    pseudocode_block_content(builder_, level_ + 1);
    return true;
  }

  // END_PSEUDOCODE_BLOCK parameter*
  private static boolean pseudocode_block_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pseudocode_block_4")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, END_PSEUDOCODE_BLOCK);
    result_ = result_ && pseudocode_block_4_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // parameter*
  private static boolean pseudocode_block_4_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pseudocode_block_4_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!parameter(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "pseudocode_block_4_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // no_math_content*
  public static boolean pseudocode_block_content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pseudocode_block_content")) return false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PSEUDOCODE_BLOCK_CONTENT, "<pseudocode block content>");
    while (true) {
      int pos_ = current_position_(builder_);
      if (!no_math_content(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "pseudocode_block_content", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // RAW_TEXT_TOKEN+
  public static boolean raw_text(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "raw_text")) return false;
    if (!nextTokenIs(builder_, RAW_TEXT_TOKEN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RAW_TEXT_TOKEN);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, RAW_TEXT_TOKEN)) break;
      if (!empty_element_parsed_guard_(builder_, "raw_text", pos_)) break;
    }
    exit_section_(builder_, marker_, RAW_TEXT, result_);
    return result_;
  }

  /* ********************************************************** */
  // OPEN_BRACE (strict_key_val_pair (COMMA strict_key_val_pair)* CLOSE_BRACE | required_param_content* CLOSE_BRACE)
  public static boolean required_param(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "required_param")) return false;
    if (!nextTokenIs(builder_, OPEN_BRACE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, REQUIRED_PARAM, null);
    result_ = consumeToken(builder_, OPEN_BRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && required_param_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // strict_key_val_pair (COMMA strict_key_val_pair)* CLOSE_BRACE | required_param_content* CLOSE_BRACE
  private static boolean required_param_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "required_param_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = required_param_1_0(builder_, level_ + 1);
    if (!result_) result_ = required_param_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // strict_key_val_pair (COMMA strict_key_val_pair)* CLOSE_BRACE
  private static boolean required_param_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "required_param_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = strict_key_val_pair(builder_, level_ + 1);
    result_ = result_ && required_param_1_0_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, CLOSE_BRACE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA strict_key_val_pair)*
  private static boolean required_param_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "required_param_1_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!required_param_1_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "required_param_1_0_1", pos_)) break;
    }
    return true;
  }

  // COMMA strict_key_val_pair
  private static boolean required_param_1_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "required_param_1_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && strict_key_val_pair(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // required_param_content* CLOSE_BRACE
  private static boolean required_param_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "required_param_1_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = required_param_1_1_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, CLOSE_BRACE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // required_param_content*
  private static boolean required_param_1_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "required_param_1_1_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!required_param_content(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "required_param_1_1_0", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // raw_text | magic_comment | comment | environment | pseudocode_block | math_environment | COMMAND_IFNEXTCHAR | group | OPEN_PAREN | CLOSE_PAREN | parameter_text | COMMA | EQUALS | OPEN_BRACKET | CLOSE_BRACKET | BACKSLASH | OPEN_ANGLE_BRACKET | CLOSE_ANGLE_BRACKET
  public static boolean required_param_content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "required_param_content")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, REQUIRED_PARAM_CONTENT, "<required param content>");
    result_ = raw_text(builder_, level_ + 1);
    if (!result_) result_ = magic_comment(builder_, level_ + 1);
    if (!result_) result_ = comment(builder_, level_ + 1);
    if (!result_) result_ = environment(builder_, level_ + 1);
    if (!result_) result_ = pseudocode_block(builder_, level_ + 1);
    if (!result_) result_ = math_environment(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, COMMAND_IFNEXTCHAR);
    if (!result_) result_ = group(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, OPEN_PAREN);
    if (!result_) result_ = consumeToken(builder_, CLOSE_PAREN);
    if (!result_) result_ = parameter_text(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = consumeToken(builder_, EQUALS);
    if (!result_) result_ = consumeToken(builder_, OPEN_BRACKET);
    if (!result_) result_ = consumeToken(builder_, CLOSE_BRACKET);
    if (!result_) result_ = consumeToken(builder_, BACKSLASH);
    if (!result_) result_ = consumeToken(builder_, OPEN_ANGLE_BRACKET);
    if (!result_) result_ = consumeToken(builder_, CLOSE_ANGLE_BRACKET);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // key_val_key EQUALS key_val_value?
  public static boolean strict_key_val_pair(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "strict_key_val_pair")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STRICT_KEY_VAL_PAIR, "<strict key val pair>");
    result_ = key_val_key(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, EQUALS);
    result_ = result_ && strict_key_val_pair_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // key_val_value?
  private static boolean strict_key_val_pair_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "strict_key_val_pair_2")) return false;
    key_val_value(builder_, level_ + 1);
    return true;
  }

}
