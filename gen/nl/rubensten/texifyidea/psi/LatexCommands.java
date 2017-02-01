// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface LatexCommands extends PsiElement {

  @NotNull
  List<LatexParameter> getParameterList();

  @NotNull
  PsiElement getCommandToken();

}
