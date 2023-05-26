// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static nl.hannahsten.texifyidea.psi.BibtexTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class BibtexParser implements PsiParser, LightPsiParser {

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
    return bibtexFile(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // (entry | comment)*
  static boolean bibtexFile(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bibtexFile")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!bibtexFile_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "bibtexFile", pos_)) break;
    }
    return true;
  }

  // entry | comment
  private static boolean bibtexFile_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bibtexFile_0")) return false;
    boolean result_;
    result_ = entry(builder_, level_ + 1);
    if (!result_) result_ = comment(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // OPEN_BRACE normal_text* CLOSE_BRACE
  public static boolean braced_string(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "braced_string")) return false;
    if (!nextTokenIs(builder_, OPEN_BRACE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BRACED_STRING, null);
    result_ = consumeToken(builder_, OPEN_BRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, braced_string_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, CLOSE_BRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // normal_text*
  private static boolean braced_string_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "braced_string_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!normal_text(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "braced_string_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // OPEN_BRACE raw_text* CLOSE_BRACE
  public static boolean braced_verbatim(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "braced_verbatim")) return false;
    if (!nextTokenIs(builder_, OPEN_BRACE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OPEN_BRACE);
    result_ = result_ && braced_verbatim_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, CLOSE_BRACE);
    exit_section_(builder_, marker_, BRACED_VERBATIM, result_);
    return result_;
  }

  // raw_text*
  private static boolean braced_verbatim_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "braced_verbatim_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!raw_text(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "braced_verbatim_1", pos_)) break;
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
  // (string (CONCATENATE string)+) | string | NUMBER | key
  public static boolean content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "content")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONTENT, "<content>");
    result_ = content_0(builder_, level_ + 1);
    if (!result_) result_ = string(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, NUMBER);
    if (!result_) result_ = key(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // string (CONCATENATE string)+
  private static boolean content_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "content_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = string(builder_, level_ + 1);
    result_ = result_ && content_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (CONCATENATE string)+
  private static boolean content_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "content_0_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = content_0_1_0(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!content_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "content_0_1", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // CONCATENATE string
  private static boolean content_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "content_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CONCATENATE);
    result_ = result_ && string(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // key
  public static boolean defined_string(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "defined_string")) return false;
    if (!nextTokenIs(builder_, "<defined string>", IDENTIFIER, VERBATIM_IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DEFINED_STRING, "<defined string>");
    result_ = key(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // SEPARATOR? comment* (CLOSE_BRACE | CLOSE_PARENTHESIS)
  public static boolean endtry(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "endtry")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ENDTRY, "<endtry>");
    result_ = endtry_0(builder_, level_ + 1);
    result_ = result_ && endtry_1(builder_, level_ + 1);
    result_ = result_ && endtry_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SEPARATOR?
  private static boolean endtry_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "endtry_0")) return false;
    consumeToken(builder_, SEPARATOR);
    return true;
  }

  // comment*
  private static boolean endtry_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "endtry_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!comment(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "endtry_1", pos_)) break;
    }
    return true;
  }

  // CLOSE_BRACE | CLOSE_PARENTHESIS
  private static boolean endtry_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "endtry_2")) return false;
    boolean result_;
    result_ = consumeToken(builder_, CLOSE_BRACE);
    if (!result_) result_ = consumeToken(builder_, CLOSE_PARENTHESIS);
    return result_;
  }

  /* ********************************************************** */
  // type (OPEN_BRACE | OPEN_PARENTHESIS) ((id SEPARATOR)? entry_content | preamble) comment* endtry comment* SEPARATOR?
  public static boolean entry(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entry")) return false;
    if (!nextTokenIs(builder_, TYPE_TOKEN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = type(builder_, level_ + 1);
    result_ = result_ && entry_1(builder_, level_ + 1);
    result_ = result_ && entry_2(builder_, level_ + 1);
    result_ = result_ && entry_3(builder_, level_ + 1);
    result_ = result_ && endtry(builder_, level_ + 1);
    result_ = result_ && entry_5(builder_, level_ + 1);
    result_ = result_ && entry_6(builder_, level_ + 1);
    exit_section_(builder_, marker_, ENTRY, result_);
    return result_;
  }

  // OPEN_BRACE | OPEN_PARENTHESIS
  private static boolean entry_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entry_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, OPEN_BRACE);
    if (!result_) result_ = consumeToken(builder_, OPEN_PARENTHESIS);
    return result_;
  }

  // (id SEPARATOR)? entry_content | preamble
  private static boolean entry_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entry_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = entry_2_0(builder_, level_ + 1);
    if (!result_) result_ = preamble(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (id SEPARATOR)? entry_content
  private static boolean entry_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entry_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = entry_2_0_0(builder_, level_ + 1);
    result_ = result_ && entry_content(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (id SEPARATOR)?
  private static boolean entry_2_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entry_2_0_0")) return false;
    entry_2_0_0_0(builder_, level_ + 1);
    return true;
  }

  // id SEPARATOR
  private static boolean entry_2_0_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entry_2_0_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = id(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEPARATOR);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // comment*
  private static boolean entry_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entry_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!comment(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "entry_3", pos_)) break;
    }
    return true;
  }

  // comment*
  private static boolean entry_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entry_5")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!comment(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "entry_5", pos_)) break;
    }
    return true;
  }

  // SEPARATOR?
  private static boolean entry_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entry_6")) return false;
    consumeToken(builder_, SEPARATOR);
    return true;
  }

  /* ********************************************************** */
  // tag (SEPARATOR tag)* SEPARATOR?
  public static boolean entry_content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entry_content")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ENTRY_CONTENT, "<entry content>");
    result_ = tag(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, entry_content_1(builder_, level_ + 1));
    result_ = pinned_ && entry_content_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (SEPARATOR tag)*
  private static boolean entry_content_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entry_content_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!entry_content_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "entry_content_1", pos_)) break;
    }
    return true;
  }

  // SEPARATOR tag
  private static boolean entry_content_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entry_content_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEPARATOR);
    result_ = result_ && tag(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // SEPARATOR?
  private static boolean entry_content_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entry_content_2")) return false;
    consumeToken(builder_, SEPARATOR);
    return true;
  }

  /* ********************************************************** */
  // comment* key comment*
  public static boolean id(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "id")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ID, "<id>");
    result_ = id_0(builder_, level_ + 1);
    result_ = result_ && key(builder_, level_ + 1);
    result_ = result_ && id_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // comment*
  private static boolean id_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "id_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!comment(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "id_0", pos_)) break;
    }
    return true;
  }

  // comment*
  private static boolean id_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "id_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!comment(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "id_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // VERBATIM_IDENTIFIER | IDENTIFIER
  public static boolean key(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "key")) return false;
    if (!nextTokenIs(builder_, "<key>", IDENTIFIER, VERBATIM_IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, KEY, "<key>");
    result_ = consumeToken(builder_, VERBATIM_IDENTIFIER);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // NORMAL_TEXT_WORD+
  public static boolean normal_text(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "normal_text")) return false;
    if (!nextTokenIs(builder_, NORMAL_TEXT_WORD)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, NORMAL_TEXT_WORD);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, NORMAL_TEXT_WORD)) break;
      if (!empty_element_parsed_guard_(builder_, "normal_text", pos_)) break;
    }
    exit_section_(builder_, marker_, NORMAL_TEXT, result_);
    return result_;
  }

  /* ********************************************************** */
  // (quoted_string (CONCATENATE quoted_string)*) | NUMBER | key
  public static boolean preamble(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "preamble")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PREAMBLE, "<preamble>");
    result_ = preamble_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, NUMBER);
    if (!result_) result_ = key(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // quoted_string (CONCATENATE quoted_string)*
  private static boolean preamble_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "preamble_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = quoted_string(builder_, level_ + 1);
    result_ = result_ && preamble_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (CONCATENATE quoted_string)*
  private static boolean preamble_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "preamble_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!preamble_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "preamble_0_1", pos_)) break;
    }
    return true;
  }

  // CONCATENATE quoted_string
  private static boolean preamble_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "preamble_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CONCATENATE);
    result_ = result_ && quoted_string(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // QUOTES normal_text? END_QUOTES
  public static boolean quoted_string(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "quoted_string")) return false;
    if (!nextTokenIs(builder_, QUOTES)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, QUOTED_STRING, null);
    result_ = consumeToken(builder_, QUOTES);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, quoted_string_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, END_QUOTES) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // normal_text?
  private static boolean quoted_string_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "quoted_string_1")) return false;
    normal_text(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // QUOTES raw_text* END_QUOTES
  public static boolean quoted_verbatim(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "quoted_verbatim")) return false;
    if (!nextTokenIs(builder_, QUOTES)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, QUOTES);
    result_ = result_ && quoted_verbatim_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, END_QUOTES);
    exit_section_(builder_, marker_, QUOTED_VERBATIM, result_);
    return result_;
  }

  // raw_text*
  private static boolean quoted_verbatim_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "quoted_verbatim_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!raw_text(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "quoted_verbatim_1", pos_)) break;
    }
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
  // defined_string | quoted_verbatim | braced_verbatim | quoted_string | braced_string
  public static boolean string(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STRING, "<string>");
    result_ = defined_string(builder_, level_ + 1);
    if (!result_) result_ = quoted_verbatim(builder_, level_ + 1);
    if (!result_) result_ = braced_verbatim(builder_, level_ + 1);
    if (!result_) result_ = quoted_string(builder_, level_ + 1);
    if (!result_) result_ = braced_string(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // comment* key comment* ASSIGNMENT comment* content comment*
  public static boolean tag(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tag")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TAG, "<tag>");
    result_ = tag_0(builder_, level_ + 1);
    result_ = result_ && key(builder_, level_ + 1);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, tag_2(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, ASSIGNMENT)) && result_;
    result_ = pinned_ && report_error_(builder_, tag_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, content(builder_, level_ + 1)) && result_;
    result_ = pinned_ && tag_6(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // comment*
  private static boolean tag_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tag_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!comment(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "tag_0", pos_)) break;
    }
    return true;
  }

  // comment*
  private static boolean tag_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tag_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!comment(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "tag_2", pos_)) break;
    }
    return true;
  }

  // comment*
  private static boolean tag_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tag_4")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!comment(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "tag_4", pos_)) break;
    }
    return true;
  }

  // comment*
  private static boolean tag_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tag_6")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!comment(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "tag_6", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // TYPE_TOKEN
  public static boolean type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type")) return false;
    if (!nextTokenIs(builder_, TYPE_TOKEN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, TYPE_TOKEN);
    exit_section_(builder_, marker_, TYPE, result_);
    return result_;
  }

}
