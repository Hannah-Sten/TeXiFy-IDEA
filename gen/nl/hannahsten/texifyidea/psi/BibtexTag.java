// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BibtexTag extends PsiElement {

  @NotNull
  List<BibtexComment> getCommentList();

  @Nullable
  BibtexContent getContent();

  @NotNull
  BibtexKey getKey();

  PsiReference getReference();

}
