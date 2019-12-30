// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import nl.hannahsten.texifyidea.index.stub.BibtexIdStub;

public interface BibtexId extends PsiNamedElement, StubBasedPsiElement<BibtexIdStub> {

  @NotNull
  List<BibtexComment> getCommentList();

}
