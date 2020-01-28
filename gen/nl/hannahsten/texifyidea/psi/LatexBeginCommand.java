// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface LatexBeginCommand extends PsiElement {

  @NotNull
  List<LatexParameter> getParameterList();

  List<String> getOptionalParameters();

  List<String> getRequiredParameters();

}
