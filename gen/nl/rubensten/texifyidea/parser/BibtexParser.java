// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static nl.rubensten.texifyidea.psi.BibtexTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class BibtexParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == BRACED_STRING) {
      r = braced_string(b, 0);
    }
    else if (t == COMMENT) {
      r = comment(b, 0);
    }
    else if (t == CONTENT) {
      r = content(b, 0);
    }
    else if (t == DEFINED_STRING) {
      r = defined_string(b, 0);
    }
    else if (t == ENDTRY) {
      r = endtry(b, 0);
    }
    else if (t == ENTRY) {
      r = entry(b, 0);
    }
    else if (t == ENTRY_CONTENT) {
      r = entry_content(b, 0);
    }
    else if (t == ID) {
      r = id(b, 0);
    }
    else if (t == KEY) {
      r = key(b, 0);
    }
    else if (t == NORMAL_TEXT) {
      r = normal_text(b, 0);
    }
    else if (t == PREAMBLE) {
      r = preamble(b, 0);
    }
    else if (t == QUOTED_STRING) {
      r = quoted_string(b, 0);
    }
    else if (t == STRING) {
      r = string(b, 0);
    }
    else if (t == TAG) {
      r = tag(b, 0);
    }
    else if (t == TYPE) {
      r = type(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return bibtexFile(b, l + 1);
  }

  /* ********************************************************** */
  // (entry | comment)*
  static boolean bibtexFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bibtexFile")) return false;
    int c = current_position_(b);
    while (true) {
      if (!bibtexFile_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "bibtexFile", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // entry | comment
  private static boolean bibtexFile_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bibtexFile_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = entry(b, l + 1);
    if (!r) r = comment(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // OPEN_BRACE normal_text+ CLOSE_BRACE
  public static boolean braced_string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "braced_string")) return false;
    if (!nextTokenIs(b, OPEN_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPEN_BRACE);
    r = r && braced_string_1(b, l + 1);
    r = r && consumeToken(b, CLOSE_BRACE);
    exit_section_(b, m, BRACED_STRING, r);
    return r;
  }

  // normal_text+
  private static boolean braced_string_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "braced_string_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = normal_text(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!normal_text(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "braced_string_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
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
  // (string (CONCATENATE string)+) | string | NUMBER | IDENTIFIER
  public static boolean content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONTENT, "<content>");
    r = content_0(b, l + 1);
    if (!r) r = string(b, l + 1);
    if (!r) r = consumeToken(b, NUMBER);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // string (CONCATENATE string)+
  private static boolean content_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "content_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = string(b, l + 1);
    r = r && content_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (CONCATENATE string)+
  private static boolean content_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "content_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = content_0_1_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!content_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "content_0_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // CONCATENATE string
  private static boolean content_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "content_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CONCATENATE);
    r = r && string(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean defined_string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defined_string")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, DEFINED_STRING, r);
    return r;
  }

  /* ********************************************************** */
  // SEPARATOR? comment* (CLOSE_BRACE | CLOSE_PARENTHESIS)
  public static boolean endtry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "endtry")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENDTRY, "<endtry>");
    r = endtry_0(b, l + 1);
    r = r && endtry_1(b, l + 1);
    r = r && endtry_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SEPARATOR?
  private static boolean endtry_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "endtry_0")) return false;
    consumeToken(b, SEPARATOR);
    return true;
  }

  // comment*
  private static boolean endtry_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "endtry_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!comment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "endtry_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // CLOSE_BRACE | CLOSE_PARENTHESIS
  private static boolean endtry_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "endtry_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CLOSE_BRACE);
    if (!r) r = consumeToken(b, CLOSE_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // type (OPEN_BRACE | OPEN_PARENTHESIS) (id? entry_content | preamble) comment* endtry comment* SEPARATOR?
  public static boolean entry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry")) return false;
    if (!nextTokenIs(b, TYPE_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type(b, l + 1);
    r = r && entry_1(b, l + 1);
    r = r && entry_2(b, l + 1);
    r = r && entry_3(b, l + 1);
    r = r && endtry(b, l + 1);
    r = r && entry_5(b, l + 1);
    r = r && entry_6(b, l + 1);
    exit_section_(b, m, ENTRY, r);
    return r;
  }

  // OPEN_BRACE | OPEN_PARENTHESIS
  private static boolean entry_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPEN_BRACE);
    if (!r) r = consumeToken(b, OPEN_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  // id? entry_content | preamble
  private static boolean entry_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = entry_2_0(b, l + 1);
    if (!r) r = preamble(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // id? entry_content
  private static boolean entry_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = entry_2_0_0(b, l + 1);
    r = r && entry_content(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // id?
  private static boolean entry_2_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry_2_0_0")) return false;
    id(b, l + 1);
    return true;
  }

  // comment*
  private static boolean entry_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!comment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "entry_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // comment*
  private static boolean entry_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry_5")) return false;
    int c = current_position_(b);
    while (true) {
      if (!comment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "entry_5", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // SEPARATOR?
  private static boolean entry_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry_6")) return false;
    consumeToken(b, SEPARATOR);
    return true;
  }

  /* ********************************************************** */
  // tag (SEPARATOR tag)* SEPARATOR?
  public static boolean entry_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry_content")) return false;
    if (!nextTokenIs(b, "<entry content>", COMMENT_TOKEN, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENTRY_CONTENT, "<entry content>");
    r = tag(b, l + 1);
    r = r && entry_content_1(b, l + 1);
    r = r && entry_content_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (SEPARATOR tag)*
  private static boolean entry_content_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry_content_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!entry_content_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "entry_content_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // SEPARATOR tag
  private static boolean entry_content_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry_content_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEPARATOR);
    r = r && tag(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SEPARATOR?
  private static boolean entry_content_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry_content_2")) return false;
    consumeToken(b, SEPARATOR);
    return true;
  }

  /* ********************************************************** */
  // comment* IDENTIFIER comment* SEPARATOR
  public static boolean id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id")) return false;
    if (!nextTokenIs(b, "<id>", COMMENT_TOKEN, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ID, "<id>");
    r = id_0(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    r = r && id_2(b, l + 1);
    r = r && consumeToken(b, SEPARATOR);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // comment*
  private static boolean id_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!comment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "id_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // comment*
  private static boolean id_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!comment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "id_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "key")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, KEY, r);
    return r;
  }

  /* ********************************************************** */
  // NORMAL_TEXT_WORD+
  public static boolean normal_text(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_text")) return false;
    if (!nextTokenIs(b, NORMAL_TEXT_WORD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NORMAL_TEXT_WORD);
    int c = current_position_(b);
    while (r) {
      if (!consumeToken(b, NORMAL_TEXT_WORD)) break;
      if (!empty_element_parsed_guard_(b, "normal_text", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, NORMAL_TEXT, r);
    return r;
  }

  /* ********************************************************** */
  // (quoted_string (CONCATENATE quoted_string)+) | quoted_string | NUMBER | IDENTIFIER
  public static boolean preamble(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "preamble")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PREAMBLE, "<preamble>");
    r = preamble_0(b, l + 1);
    if (!r) r = quoted_string(b, l + 1);
    if (!r) r = consumeToken(b, NUMBER);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // quoted_string (CONCATENATE quoted_string)+
  private static boolean preamble_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "preamble_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = quoted_string(b, l + 1);
    r = r && preamble_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (CONCATENATE quoted_string)+
  private static boolean preamble_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "preamble_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = preamble_0_1_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!preamble_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "preamble_0_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // CONCATENATE quoted_string
  private static boolean preamble_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "preamble_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CONCATENATE);
    r = r && quoted_string(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // QUOTES normal_text END_QUOTES
  public static boolean quoted_string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "quoted_string")) return false;
    if (!nextTokenIs(b, QUOTES)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUOTES);
    r = r && normal_text(b, l + 1);
    r = r && consumeToken(b, END_QUOTES);
    exit_section_(b, m, QUOTED_STRING, r);
    return r;
  }

  /* ********************************************************** */
  // defined_string | quoted_string | braced_string
  public static boolean string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING, "<string>");
    r = defined_string(b, l + 1);
    if (!r) r = quoted_string(b, l + 1);
    if (!r) r = braced_string(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // comment* key comment* ASSIGNMENT comment* content comment*
  public static boolean tag(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tag")) return false;
    if (!nextTokenIs(b, "<tag>", COMMENT_TOKEN, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TAG, "<tag>");
    r = tag_0(b, l + 1);
    r = r && key(b, l + 1);
    r = r && tag_2(b, l + 1);
    r = r && consumeToken(b, ASSIGNMENT);
    r = r && tag_4(b, l + 1);
    r = r && content(b, l + 1);
    r = r && tag_6(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // comment*
  private static boolean tag_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tag_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!comment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "tag_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // comment*
  private static boolean tag_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tag_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!comment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "tag_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // comment*
  private static boolean tag_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tag_4")) return false;
    int c = current_position_(b);
    while (true) {
      if (!comment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "tag_4", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // comment*
  private static boolean tag_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tag_6")) return false;
    int c = current_position_(b);
    while (true) {
      if (!comment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "tag_6", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // TYPE_TOKEN
  public static boolean type(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type")) return false;
    if (!nextTokenIs(b, TYPE_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TYPE_TOKEN);
    exit_section_(b, m, TYPE, r);
    return r;
  }

}
