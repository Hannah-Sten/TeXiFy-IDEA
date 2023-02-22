// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.StubBasedPsiElement;
import nl.hannahsten.texifyidea.index.stub.BibtexEntryStub;
import com.intellij.psi.PsiReference;

public interface BibtexEntry extends PsiNameIdentifierOwner, StubBasedPsiElement<BibtexEntryStub> {

  @NotNull
  List<BibtexComment> getCommentList();

  @NotNull
  BibtexEndtry getEndtry();

  @Nullable
  BibtexEntryContent getEntryContent();

  @Nullable
  BibtexId getId();

  @Nullable
  BibtexPreamble getPreamble();

  @NotNull
  BibtexType getType();

  PsiReference[] getReferences();

  String getTitle();

  List<String> getAuthors();

  String getYear();

  String getIdentifier();

  PsiElement getNameIdentifier();

  String getAbstract();

  String getTagContent(String tagName);

  String getName();

  PsiElement setName(@NotNull @NonNls String name);

  boolean equals(Object other);

  int hashCode();

}
