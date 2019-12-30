// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import nl.hannahsten.texifyidea.index.stub.BibtexEntryStubElementType;
import nl.hannahsten.texifyidea.psi.impl.*;

public interface BibtexTypes {

  IElementType BRACED_STRING = new BibtexEntryStubElementType("BRACED_STRING");
  IElementType COMMENT = new BibtexEntryStubElementType("COMMENT");
  IElementType CONTENT = new BibtexEntryStubElementType("CONTENT");
  IElementType DEFINED_STRING = new BibtexEntryStubElementType("DEFINED_STRING");
  IElementType ENDTRY = new BibtexEntryStubElementType("ENDTRY");
  IElementType ENTRY = new BibtexEntryStubElementType("ENTRY");
  IElementType ENTRY_CONTENT = new BibtexEntryStubElementType("ENTRY_CONTENT");
  IElementType ID = new BibtexEntryStubElementType("ID");
  IElementType KEY = new BibtexEntryStubElementType("KEY");
  IElementType NORMAL_TEXT = new BibtexEntryStubElementType("NORMAL_TEXT");
  IElementType PREAMBLE = new BibtexEntryStubElementType("PREAMBLE");
  IElementType QUOTED_STRING = new BibtexEntryStubElementType("QUOTED_STRING");
  IElementType STRING = new BibtexEntryStubElementType("STRING");
  IElementType TAG = new BibtexEntryStubElementType("TAG");
  IElementType TYPE = new BibtexEntryStubElementType("TYPE");

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
  IElementType SEPARATOR = new BibtexTokenType("SEPARATOR");
  IElementType TYPE_TOKEN = new BibtexTokenType("TYPE_TOKEN");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == BRACED_STRING) {
        return new BibtexBracedStringImpl(node);
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
