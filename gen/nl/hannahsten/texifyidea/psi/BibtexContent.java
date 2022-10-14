// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface BibtexContent extends PsiElement {

  @Nullable
  BibtexKey getKey();

  @NotNull
  List<BibtexString> getStringList();

}
