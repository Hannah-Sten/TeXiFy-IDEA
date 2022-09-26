// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface LatexParameterGroupText extends PsiElement {

  @NotNull
  List<LatexGroup> getGroupList();

  @NotNull
  List<LatexParameterText> getParameterTextList();

}
