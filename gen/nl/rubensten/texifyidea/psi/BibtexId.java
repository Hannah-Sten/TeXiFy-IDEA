// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.psi;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import nl.rubensten.texifyidea.index.stub.BibtexIdStub;
import nl.rubensten.texifyidea.util.StringUtilKt;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BibtexId extends StubBasedPsiElement<BibtexIdStub>, PsiNamedElement {

    @NotNull
    List<BibtexComment> getCommentList();

    default String getName() {
        return StringUtilKt.substringEnd(getText(), 1);
    }
}
