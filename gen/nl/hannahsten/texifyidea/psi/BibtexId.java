// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import nl.hannahsten.texifyidea.index.stub.BibtexIdStub;
import nl.hannahsten.texifyidea.util.StringsKt;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BibtexId extends StubBasedPsiElement<BibtexIdStub>, PsiNamedElement {

  @NotNull
  List<BibtexComment> getCommentList();

  default String getName() {
    return StringsKt.substringEnd(getText(), 1);
  }
}
