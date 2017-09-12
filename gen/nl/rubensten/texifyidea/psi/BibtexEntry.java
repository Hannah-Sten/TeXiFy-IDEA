// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface BibtexEntry extends PsiElement {

  @Nullable
  BibtexId getId();

  @Nullable
  BibtexPreamble getPreamble();

  @NotNull
  List<BibtexTag> getTagList();

}
