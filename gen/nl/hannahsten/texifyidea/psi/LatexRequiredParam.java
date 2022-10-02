// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface LatexRequiredParam extends PsiElement {

  @NotNull
  List<LatexRequiredParamContent> getRequiredParamContentList();

  @NotNull
  List<LatexStrictKeyvalPair> getStrictKeyvalPairList();

}
