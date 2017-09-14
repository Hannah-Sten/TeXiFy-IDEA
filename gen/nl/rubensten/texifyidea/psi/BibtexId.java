// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.psi;

import java.util.List;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import nl.rubensten.texifyidea.index.stub.BibtexIdStub;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface BibtexId extends StubBasedPsiElement<BibtexIdStub>, PsiNamedElement {

  @NotNull
  List<BibtexComment> getCommentList();

}
