// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LatexParameter extends PsiLanguageInjectionHost {

  @Nullable
  LatexAngleParam getAngleParam();

  @Nullable
  LatexOptionalParam getOptionalParam();

  @Nullable
  LatexPictureParam getPictureParam();

  @Nullable
  LatexRequiredParam getRequiredParam();

  boolean isValidHost();

  PsiLanguageInjectionHost updateText(@NotNull String text);

  @NotNull
  LiteralTextEscaper<LatexParameter> createLiteralTextEscaper();

}
