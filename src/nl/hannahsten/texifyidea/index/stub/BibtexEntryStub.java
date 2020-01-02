package nl.hannahsten.texifyidea.index.stub;

import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.stubs.StubElement;
import nl.hannahsten.texifyidea.psi.BibtexEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BibtexEntryStub extends StubElement<BibtexEntry>, NamedStub<BibtexEntry> {
    String getTitle();

    List<String> getAuthors();

    String getYear();

    String getIdentifier();

    @Nullable
    @Override
    default String getName() {
        return getIdentifier();
    }
}
