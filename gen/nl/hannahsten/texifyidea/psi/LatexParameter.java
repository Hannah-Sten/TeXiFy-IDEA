// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;

public interface LatexParameter extends PsiLanguageInjectionHost {

  @Nullable
  LatexOptionalParam getOptionalParam();

  @Nullable
  LatexPictureParam getPictureParam();

  @Nullable
  LatexRequiredParam getRequiredParam();

}
