// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import nl.rubensten.texifyidea.psi.impl.LatexCommandImpl;
import nl.rubensten.texifyidea.psi.impl.LatexCommentImpl;
import nl.rubensten.texifyidea.psi.impl.LatexContentImpl;
import nl.rubensten.texifyidea.psi.impl.LatexDisplayMathImpl;
import nl.rubensten.texifyidea.psi.impl.LatexGroupImpl;
import nl.rubensten.texifyidea.psi.impl.LatexInlineMathImpl;
import nl.rubensten.texifyidea.psi.impl.LatexMathEnvironmentImpl;
import nl.rubensten.texifyidea.psi.impl.LatexNoMathContentImpl;
import nl.rubensten.texifyidea.psi.impl.LatexOpenGroupImpl;
import nl.rubensten.texifyidea.psi.impl.LatexOptionalParamImpl;
import nl.rubensten.texifyidea.psi.impl.LatexParameterImpl;
import nl.rubensten.texifyidea.psi.impl.LatexRequiredParamImpl;

public interface LatexTypes {

  IElementType COMMAND = new LatexElementType("COMMAND");
  IElementType COMMENT = new LatexElementType("COMMENT");
  IElementType CONTENT = new LatexElementType("CONTENT");
  IElementType DISPLAY_MATH = new LatexElementType("DISPLAY_MATH");
  IElementType GROUP = new LatexElementType("GROUP");
  IElementType INLINE_MATH = new LatexElementType("INLINE_MATH");
  IElementType MATH_ENVIRONMENT = new LatexElementType("MATH_ENVIRONMENT");
  IElementType NO_MATH_CONTENT = new LatexElementType("NO_MATH_CONTENT");
  IElementType OPEN_GROUP = new LatexElementType("OPEN_GROUP");
  IElementType OPTIONAL_PARAM = new LatexElementType("OPTIONAL_PARAM");
  IElementType PARAMETER = new LatexElementType("PARAMETER");
  IElementType REQUIRED_PARAM = new LatexElementType("REQUIRED_PARAM");

  IElementType CLOSE_BRACE = new LatexTokenType("}");
  IElementType CLOSE_BRACKET = new LatexTokenType("]");
  IElementType COMMAND_TOKEN = new LatexTokenType("COMMAND_TOKEN");
  IElementType COMMENT_TOKEN = new LatexTokenType("COMMENT_TOKEN");
  IElementType DISPLAY_MATH_END = new LatexTokenType("\\]");
  IElementType DISPLAY_MATH_START = new LatexTokenType("\\[");
  IElementType INLINE_MATH_DELIM = new LatexTokenType("$");
  IElementType NORMAL_TEXT = new LatexTokenType("NORMAL_TEXT");
  IElementType OPEN_BRACE = new LatexTokenType("{");
  IElementType OPEN_BRACKET = new LatexTokenType("[");
  IElementType STAR = new LatexTokenType("*");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == COMMAND) {
        return new LatexCommandImpl(node);
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
      else if (type == GROUP) {
        return new LatexGroupImpl(node);
      }
      else if (type == INLINE_MATH) {
        return new LatexInlineMathImpl(node);
      }
      else if (type == MATH_ENVIRONMENT) {
        return new LatexMathEnvironmentImpl(node);
      }
      else if (type == NO_MATH_CONTENT) {
        return new LatexNoMathContentImpl(node);
      }
      else if (type == OPEN_GROUP) {
        return new LatexOpenGroupImpl(node);
      }
      else if (type == OPTIONAL_PARAM) {
        return new LatexOptionalParamImpl(node);
      }
      else if (type == PARAMETER) {
        return new LatexParameterImpl(node);
      }
      else if (type == REQUIRED_PARAM) {
        return new LatexRequiredParamImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
