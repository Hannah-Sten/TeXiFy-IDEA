package nl.hannahsten.texifyidea.index.stub;

import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.stubs.StubElement;
import nl.hannahsten.texifyidea.psi.BibtexId;
import org.jetbrains.annotations.Nullable;

/**
 * @author Hannah Schellekens
 */
public interface BibtexIdStub extends StubElement<BibtexId>, NamedStub<BibtexId> {

    String getIdentifier();

    @Nullable
    @Override
    default String getName() {
        return getIdentifier();
    }
}
