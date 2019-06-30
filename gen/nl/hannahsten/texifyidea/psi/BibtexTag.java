// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface BibtexTag extends PsiElement {

  @NotNull
  List<BibtexComment> getCommentList();

  @Nullable
  BibtexContent getContent();

  @NotNull
  BibtexKey getKey();

}
