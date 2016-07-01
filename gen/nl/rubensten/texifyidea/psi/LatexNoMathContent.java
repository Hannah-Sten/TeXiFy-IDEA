// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface LatexNoMathContent extends PsiElement {

  @Nullable
  LatexCommand getCommand();

  @Nullable
  LatexComment getComment();

  @Nullable
  LatexGroup getGroup();

  @Nullable
  LatexOpenGroup getOpenGroup();

  @Nullable
  PsiElement getNormalText();

}
