// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import nl.hannahsten.texifyidea.index.stub.BibtexEntryStubElementType;
import nl.hannahsten.texifyidea.psi.impl.*;

public interface BibtexTypes {

  IElementType BRACED_STRING = new BibtexElementType("BRACED_STRING");
  IElementType BRACED_VERBATIM = new BibtexElementType("BRACED_VERBATIM");
  IElementType COMMENT = new BibtexElementType("COMMENT");
  IElementType CONTENT = new BibtexElementType("CONTENT");
  IElementType DEFINED_STRING = new BibtexElementType("DEFINED_STRING");
  IElementType ENDTRY = new BibtexElementType("ENDTRY");
  IElementType ENTRY = new BibtexEntryStubElementType("ENTRY");
  IElementType ENTRY_CONTENT = new BibtexElementType("ENTRY_CONTENT");
  IElementType ID = new BibtexElementType("ID");
  IElementType KEY = new BibtexElementType("KEY");
  IElementType NORMAL_TEXT = new BibtexElementType("NORMAL_TEXT");
  IElementType PREAMBLE = new BibtexElementType("PREAMBLE");
  IElementType QUOTED_STRING = new BibtexElementType("QUOTED_STRING");
  IElementType QUOTED_VERBATIM = new BibtexElementType("QUOTED_VERBATIM");
  IElementType RAW_TEXT = new BibtexElementType("RAW_TEXT");
  IElementType STRING = new BibtexElementType("STRING");
  IElementType TAG = new BibtexElementType("TAG");
  IElementType TYPE = new BibtexElementType("TYPE");

  IElementType ASSIGNMENT = new BibtexTokenType("ASSIGNMENT");
  IElementType CLOSE_BRACE = new BibtexTokenType("CLOSE_BRACE");
  IElementType CLOSE_PARENTHESIS = new BibtexTokenType("CLOSE_PARENTHESIS");
  IElementType COMMENT_TOKEN = new BibtexTokenType("COMMENT_TOKEN");
  IElementType CONCATENATE = new BibtexTokenType("CONCATENATE");
  IElementType END_QUOTES = new BibtexTokenType("END_QUOTES");
  IElementType IDENTIFIER = new BibtexTokenType("IDENTIFIER");
  IElementType NORMAL_TEXT_WORD = new BibtexTokenType("NORMAL_TEXT_WORD");
  IElementType NUMBER = new BibtexTokenType("NUMBER");
  IElementType OPEN_BRACE = new BibtexTokenType("OPEN_BRACE");
  IElementType OPEN_PARENTHESIS = new BibtexTokenType("OPEN_PARENTHESIS");
  IElementType QUOTES = new BibtexTokenType("QUOTES");
  IElementType RAW_TEXT_TOKEN = new BibtexTokenType("RAW_TEXT");
  IElementType SEPARATOR = new BibtexTokenType("SEPARATOR");
  IElementType TYPE_TOKEN = new BibtexTokenType("TYPE_TOKEN");
  IElementType VERBATIM_IDENTIFIER = new BibtexTokenType("VERBATIM_IDENTIFIER");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == BRACED_STRING) {
        return new BibtexBracedStringImpl(node);
      }
      else if (type == BRACED_VERBATIM) {
        return new BibtexBracedVerbatimImpl(node);
      }
      else if (type == COMMENT) {
        return new BibtexCommentImpl(node);
      }
      else if (type == CONTENT) {
        return new BibtexContentImpl(node);
      }
      else if (type == DEFINED_STRING) {
        return new BibtexDefinedStringImpl(node);
      }
      else if (type == ENDTRY) {
        return new BibtexEndtryImpl(node);
      }
      else if (type == ENTRY) {
        return new BibtexEntryImpl(node);
      }
      else if (type == ENTRY_CONTENT) {
        return new BibtexEntryContentImpl(node);
      }
      else if (type == ID) {
        return new BibtexIdImpl(node);
      }
      else if (type == KEY) {
        return new BibtexKeyImpl(node);
      }
      else if (type == NORMAL_TEXT) {
        return new BibtexNormalTextImpl(node);
      }
      else if (type == PREAMBLE) {
        return new BibtexPreambleImpl(node);
      }
      else if (type == QUOTED_STRING) {
        return new BibtexQuotedStringImpl(node);
      }
      else if (type == QUOTED_VERBATIM) {
        return new BibtexQuotedVerbatimImpl(node);
      }
      else if (type == RAW_TEXT) {
        return new BibtexRawTextImpl(node);
      }
      else if (type == STRING) {
        return new BibtexStringImpl(node);
      }
      else if (type == TAG) {
        return new BibtexTagImpl(node);
      }
      else if (type == TYPE) {
        return new BibtexTypeImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
