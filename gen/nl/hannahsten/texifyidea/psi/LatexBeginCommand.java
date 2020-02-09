// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import java.util.LinkedHashMap;

public interface LatexBeginCommand extends PsiElement {

  @NotNull
  List<LatexParameter> getParameterList();

  LinkedHashMap<String, String> getOptionalParameters();

  List<String> getRequiredParameters();

}
