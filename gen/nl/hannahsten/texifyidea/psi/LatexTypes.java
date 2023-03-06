// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStubElementType;
import nl.hannahsten.texifyidea.index.stub.LatexEnvironmentStubElementType;
import nl.hannahsten.texifyidea.index.stub.LatexMagicCommentStubElementType;
import nl.hannahsten.texifyidea.psi.impl.*;

public interface LatexTypes {

  IElementType BEGIN_COMMAND = new LatexElementType("BEGIN_COMMAND");
  IElementType COMMANDS = new LatexCommandsStubElementType("COMMANDS");
  IElementType COMMENT = new LatexElementType("COMMENT");
  IElementType CONTENT = new LatexElementType("CONTENT");
  IElementType DISPLAY_MATH = new LatexElementType("DISPLAY_MATH");
  IElementType END_COMMAND = new LatexElementType("END_COMMAND");
  IElementType ENVIRONMENT = new LatexEnvironmentStubElementType("ENVIRONMENT");
  IElementType ENVIRONMENT_CONTENT = new LatexElementType("ENVIRONMENT_CONTENT");
  IElementType GROUP = new LatexElementType("GROUP");
  IElementType INLINE_MATH = new LatexElementType("INLINE_MATH");
  IElementType KEY_VAL_CONTENT = new LatexElementType("KEY_VAL_CONTENT");
  IElementType KEY_VAL_KEY = new LatexElementType("KEY_VAL_KEY");
  IElementType KEY_VAL_PAIR = new LatexElementType("KEY_VAL_PAIR");
  IElementType KEY_VAL_VALUE = new LatexElementType("KEY_VAL_VALUE");
  IElementType MAGIC_COMMENT = new LatexMagicCommentStubElementType("MAGIC_COMMENT");
  IElementType MATH_CONTENT = new LatexElementType("MATH_CONTENT");
  IElementType MATH_ENVIRONMENT = new LatexElementType("MATH_ENVIRONMENT");
  IElementType NORMAL_TEXT = new LatexElementType("NORMAL_TEXT");
  IElementType NO_MATH_CONTENT = new LatexElementType("NO_MATH_CONTENT");
  IElementType OPTIONAL_PARAM = new LatexElementType("OPTIONAL_PARAM");
  IElementType OPTIONAL_PARAM_CONTENT = new LatexElementType("OPTIONAL_PARAM_CONTENT");
  IElementType PARAMETER = new LatexElementType("PARAMETER");
  IElementType PARAMETER_GROUP = new LatexElementType("PARAMETER_GROUP");
  IElementType PARAMETER_GROUP_TEXT = new LatexElementType("PARAMETER_GROUP_TEXT");
  IElementType PARAMETER_TEXT = new LatexElementType("PARAMETER_TEXT");
  IElementType PICTURE_PARAM = new LatexElementType("PICTURE_PARAM");
  IElementType PICTURE_PARAM_CONTENT = new LatexElementType("PICTURE_PARAM_CONTENT");
  IElementType PSEUDOCODE_BLOCK = new LatexElementType("PSEUDOCODE_BLOCK");
  IElementType PSEUDOCODE_BLOCK_CONTENT = new LatexElementType("PSEUDOCODE_BLOCK_CONTENT");
  IElementType RAW_TEXT = new LatexElementType("RAW_TEXT");
  IElementType REQUIRED_PARAM = new LatexElementType("REQUIRED_PARAM");
  IElementType REQUIRED_PARAM_CONTENT = new LatexElementType("REQUIRED_PARAM_CONTENT");
  IElementType STRICT_KEY_VAL_PAIR = new LatexElementType("STRICT_KEY_VAL_PAIR");

  IElementType AMPERSAND = new LatexTokenType("&");
  IElementType ANGLE_PARAM = new LatexTokenType("ANGLE_PARAM");
  IElementType BACKSLASH = new LatexTokenType("BACKSLASH");
  IElementType BEGIN_PSEUDOCODE_BLOCK = new LatexTokenType("BEGIN_PSEUDOCODE_BLOCK");
  IElementType BEGIN_TOKEN = new LatexTokenType("\\begin");
  IElementType CLOSE_ANGLE_BRACKET = new LatexTokenType("CLOSE_ANGLE_BRACKET");
  IElementType CLOSE_BRACE = new LatexTokenType("CLOSE_BRACE");
  IElementType CLOSE_BRACKET = new LatexTokenType("CLOSE_BRACKET");
  IElementType CLOSE_PAREN = new LatexTokenType("CLOSE_PAREN");
  IElementType COMMA = new LatexTokenType("COMMA");
  IElementType COMMAND_IFNEXTCHAR = new LatexTokenType("COMMAND_IFNEXTCHAR");
  IElementType COMMAND_TOKEN = new LatexTokenType("COMMAND_TOKEN");
  IElementType COMMENT_TOKEN = new LatexTokenType("COMMENT_TOKEN");
  IElementType DISPLAY_MATH_END = new LatexTokenType("\\]");
  IElementType DISPLAY_MATH_START = new LatexTokenType("\\[");
  IElementType END_PSEUDOCODE_BLOCK = new LatexTokenType("END_PSEUDOCODE_BLOCK");
  IElementType END_TOKEN = new LatexTokenType("\\end");
  IElementType EQUALS = new LatexTokenType("EQUALS");
  IElementType EXCLAMATION_MARK = new LatexTokenType("EXCLAMATION_MARK");
  IElementType INLINE_MATH_END = new LatexTokenType("INLINE_MATH_END");
  IElementType INLINE_MATH_START = new LatexTokenType("INLINE_MATH_START");
  IElementType MAGIC_COMMENT_TOKEN = new LatexTokenType("MAGIC_COMMENT_TOKEN");
  IElementType MIDDLE_PSEUDOCODE_BLOCK = new LatexTokenType("MIDDLE_PSEUDOCODE_BLOCK");
  IElementType NORMAL_TEXT_CHAR = new LatexTokenType("NORMAL_TEXT_CHAR");
  IElementType NORMAL_TEXT_WORD = new LatexTokenType("NORMAL_TEXT_WORD");
  IElementType OPEN_ANGLE_BRACKET = new LatexTokenType("OPEN_ANGLE_BRACKET");
  IElementType OPEN_BRACE = new LatexTokenType("OPEN_BRACE");
  IElementType OPEN_BRACKET = new LatexTokenType("OPEN_BRACKET");
  IElementType OPEN_PAREN = new LatexTokenType("OPEN_PAREN");
  IElementType PIPE = new LatexTokenType("PIPE");
  IElementType QUOTATION_MARK = new LatexTokenType("QUOTATION_MARK");
  IElementType RAW_TEXT_TOKEN = new LatexTokenType("RAW_TEXT_TOKEN");
  IElementType STAR = new LatexTokenType("*");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == BEGIN_COMMAND) {
        return new LatexBeginCommandImpl(node);
      }
      else if (type == COMMANDS) {
        return new LatexCommandsImpl(node);
      }
      else if (type == COMMENT) {
        return new LatexCommentImpl(node);
      }
      else if (type == CONTENT) {
        return new LatexContentImpl(node);
      }
      else if (type == DISPLAY_MATH) {
        return new LatexDisplayMathImpl(node);
      }
      else if (type == END_COMMAND) {
        return new LatexEndCommandImpl(node);
      }
      else if (type == ENVIRONMENT) {
        return new LatexEnvironmentImpl(node);
      }
      else if (type == ENVIRONMENT_CONTENT) {
        return new LatexEnvironmentContentImpl(node);
      }
      else if (type == GROUP) {
        return new LatexGroupImpl(node);
      }
      else if (type == INLINE_MATH) {
        return new LatexInlineMathImpl(node);
      }
      else if (type == KEY_VAL_CONTENT) {
        return new LatexKeyValContentImpl(node);
      }
      else if (type == KEY_VAL_KEY) {
        return new LatexKeyValKeyImpl(node);
      }
      else if (type == KEY_VAL_PAIR) {
        return new LatexKeyValPairImpl(node);
      }
      else if (type == KEY_VAL_VALUE) {
        return new LatexKeyValValueImpl(node);
      }
      else if (type == MAGIC_COMMENT) {
        return new LatexMagicCommentImpl(node);
      }
      else if (type == MATH_CONTENT) {
        return new LatexMathContentImpl(node);
      }
      else if (type == MATH_ENVIRONMENT) {
        return new LatexMathEnvironmentImpl(node);
      }
      else if (type == NORMAL_TEXT) {
        return new LatexNormalTextImpl(node);
      }
      else if (type == NO_MATH_CONTENT) {
        return new LatexNoMathContentImpl(node);
      }
      else if (type == OPTIONAL_PARAM) {
        return new LatexOptionalParamImpl(node);
      }
      else if (type == OPTIONAL_PARAM_CONTENT) {
        return new LatexOptionalParamContentImpl(node);
      }
      else if (type == PARAMETER) {
        return new LatexParameterImpl(node);
      }
      else if (type == PARAMETER_GROUP) {
        return new LatexParameterGroupImpl(node);
      }
      else if (type == PARAMETER_GROUP_TEXT) {
        return new LatexParameterGroupTextImpl(node);
      }
      else if (type == PARAMETER_TEXT) {
        return new LatexParameterTextImpl(node);
      }
      else if (type == PICTURE_PARAM) {
        return new LatexPictureParamImpl(node);
      }
      else if (type == PICTURE_PARAM_CONTENT) {
        return new LatexPictureParamContentImpl(node);
      }
      else if (type == PSEUDOCODE_BLOCK) {
        return new LatexPseudocodeBlockImpl(node);
      }
      else if (type == PSEUDOCODE_BLOCK_CONTENT) {
        return new LatexPseudocodeBlockContentImpl(node);
      }
      else if (type == RAW_TEXT) {
        return new LatexRawTextImpl(node);
      }
      else if (type == REQUIRED_PARAM) {
        return new LatexRequiredParamImpl(node);
      }
      else if (type == REQUIRED_PARAM_CONTENT) {
        return new LatexRequiredParamContentImpl(node);
      }
      else if (type == STRICT_KEY_VAL_PAIR) {
        return new LatexStrictKeyValPairImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
