// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import nl.rubensten.texifyidea.psi.impl.*;

public interface BibtexTypes {

  IElementType BRACED_STRING = new BibtexElementType("BRACED_STRING");
  IElementType COMMENT = new BibtexElementType("COMMENT");
  IElementType CONTENT = new BibtexElementType("CONTENT");
  IElementType DEFINED_STRING = new BibtexElementType("DEFINED_STRING");
  IElementType ENTRY = new BibtexElementType("ENTRY");
  IElementType ID = new BibtexElementType("ID");
  IElementType KEY = new BibtexElementType("KEY");
  IElementType PREAMBLE = new BibtexElementType("PREAMBLE");
  IElementType QUOTED_STRING = new BibtexElementType("QUOTED_STRING");
  IElementType STRING = new BibtexElementType("STRING");
  IElementType TAG = new BibtexElementType("TAG");
  IElementType TYPE = new BibtexElementType("TYPE");

  IElementType ASSIGNMENT = new BibtexTokenType("ASSIGNMENT");
  IElementType CLOSE_BRACE = new BibtexTokenType("CLOSE_BRACE");
  IElementType COMMENT_TOKEN = new BibtexTokenType("COMMENT_TOKEN");
  IElementType CONCATENATE = new BibtexTokenType("CONCATENATE");
  IElementType ENDTRY = new BibtexTokenType("ENDTRY");
  IElementType END_QUOTES = new BibtexTokenType("END_QUOTES");
  IElementType IDENTIFIER = new BibtexTokenType("IDENTIFIER");
  IElementType NORMAL_TEXT = new BibtexTokenType("NORMAL_TEXT");
  IElementType NUMBER = new BibtexTokenType("NUMBER");
  IElementType OPEN_BRACE = new BibtexTokenType("OPEN_BRACE");
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
      else if (type == ENTRY) {
        return new BibtexEntryImpl(node);
      }
      else if (type == ID) {
        return new BibtexIdImpl(node);
      }
      else if (type == KEY) {
        return new BibtexKeyImpl(node);
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
