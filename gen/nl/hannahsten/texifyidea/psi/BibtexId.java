// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;

public interface BibtexId extends PsiNameIdentifierOwner {

  @NotNull
  List<BibtexComment> getCommentList();

  PsiElement getNameIdentifier();

  String getName();

  PsiElement setName(String name);

  void delete();

}
