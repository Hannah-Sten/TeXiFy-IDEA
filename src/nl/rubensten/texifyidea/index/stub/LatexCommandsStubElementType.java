package nl.rubensten.texifyidea.index.stub;

import com.intellij.lang.Language;
import com.intellij.psi.stubs.*;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author Ruben Schellekens
 */
public class LatexCommandsStubElementType
        extends IStubElementType<LatexCommandsStub, LatexCommands> {

    public LatexCommandsStubElementType(@NotNull String debugName, @Nullable Language language) {
        // TODO: See Properties example.
        super(debugName, language);
    }

    @Override
    public LatexCommands createPsi(@NotNull LatexCommandsStub latexCommandsStub) {
        // TODO: Implement.
        return null;
    }

    @NotNull
    @Override
    public LatexCommandsStub createStub(@NotNull LatexCommands latexCommands, StubElement stubElement) {
        // TODO: Implement.
        return null;
    }

    @NotNull
    @Override
    public String getExternalId() {
        // TODO: Implement.
        return null;
    }

    @Override
    public void serialize(@NotNull LatexCommandsStub latexCommandsStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        // TODO: Implement.
    }

    @NotNull
    @Override
    public LatexCommandsStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        // TODO: Implement.
        return null;
    }

    @Override
    public void indexStub(@NotNull LatexCommandsStub latexCommandsStub, @NotNull IndexSink indexSink) {
        // TODO: Implement.
    }
}
