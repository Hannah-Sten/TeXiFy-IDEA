// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import nl.hannahsten.texifyidea.index.stub.BibtexIdStubElementType;
import nl.hannahsten.texifyidea.psi.impl.*;

public interface BibtexTypes {

  IElementType BRACED_STRING = new BibtexIdStubElementType("BRACED_STRING");
  IElementType COMMENT = new BibtexIdStubElementType("COMMENT");
  IElementType CONTENT = new BibtexIdStubElementType("CONTENT");
  IElementType DEFINED_STRING = new BibtexIdStubElementType("DEFINED_STRING");
  IElementType ENDTRY = new BibtexIdStubElementType("ENDTRY");
  IElementType ENTRY = new BibtexIdStubElementType("ENTRY");
  IElementType ENTRY_CONTENT = new BibtexIdStubElementType("ENTRY_CONTENT");
  IElementType ID = new BibtexIdStubElementType("ID");
  IElementType KEY = new BibtexIdStubElementType("KEY");
  IElementType NORMAL_TEXT = new BibtexIdStubElementType("NORMAL_TEXT");
  IElementType PREAMBLE = new BibtexIdStubElementType("PREAMBLE");
  IElementType QUOTED_STRING = new BibtexIdStubElementType("QUOTED_STRING");
  IElementType STRING = new BibtexIdStubElementType("STRING");
  IElementType TAG = new BibtexIdStubElementType("TAG");
  IElementType TYPE = new BibtexIdStubElementType("TYPE");

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
