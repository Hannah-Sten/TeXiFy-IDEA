// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface LatexOptionalParamContent extends PsiElement {

  @Nullable
  LatexCommands getCommands();

  @Nullable
  LatexComment getComment();

  @Nullable
  LatexEnvironment getEnvironment();

  @Nullable
  LatexGroup getGroup();

  @Nullable
  LatexMagicComment getMagicComment();

  @Nullable
  LatexMathEnvironment getMathEnvironment();

  @Nullable
  LatexParameterText getParameterText();

  @Nullable
  LatexPseudocodeBlock getPseudocodeBlock();

  @Nullable
  LatexRawText getRawText();

}
