// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface LatexEnvironment extends PsiElement {

  @NotNull
  LatexBeginCommand getBeginCommand();

  @NotNull
  LatexEndCommand getEndCommand();

  @Nullable
  LatexEnvironmentContent getEnvironmentContent();

}
