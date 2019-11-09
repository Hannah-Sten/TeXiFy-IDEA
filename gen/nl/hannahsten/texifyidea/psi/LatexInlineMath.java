// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface LatexInlineMath extends PsiElement {

  @Nullable
  LatexMathContent getMathContent();

  @Nullable
  PsiElement getInlineMathEnd();

  @NotNull
  PsiElement getInlineMathStart();

}
