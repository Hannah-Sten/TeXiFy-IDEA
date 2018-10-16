package nl.rubensten.texifyidea.index.stub;

import com.intellij.psi.stubs.*;
import nl.rubensten.texifyidea.LatexLanguage;
import nl.rubensten.texifyidea.index.LatexCommandsIndex;
import nl.rubensten.texifyidea.index.LatexDefinitionIndex;
import nl.rubensten.texifyidea.index.LatexIncludesIndex;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.impl.LatexCommandsImpl;
import nl.rubensten.texifyidea.util.Magic;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Ruben Schellekens
 */
public class LatexCommandsStubElementType extends IStubElementType<LatexCommandsStub, LatexCommands> {

    private static final Pattern SEPERATOR = Pattern.compile("\u1923\u9123\u2d20 hello\u0012");

    public LatexCommandsStubElementType(@NotNull String debugName) {
        super("latex-commands", LatexLanguage.INSTANCE);
    }

    @Override
    public LatexCommands createPsi(@NotNull LatexCommandsStub latexCommandsStub) {
        return new LatexCommandsImpl(latexCommandsStub, this) {{
            setName(latexCommandsStub.getName());
        }};
    }

    @NotNull
    @Override
    public LatexCommandsStub createStub(@NotNull LatexCommands latexCommands, StubElement parent) {
        String commandToken = latexCommands.getCommandToken().getText();
        latexCommands.setName(commandToken);

        List<String> requiredParameters = latexCommands.getRequiredParameters();
        List<String> optionalParameters = latexCommands.getOptionalParameters();

        return new LatexCommandsStubImpl(
                parent, this,
                commandToken,
                requiredParameters,
                optionalParameters
        );
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "texify.latex.commands";
    }

    @Override
    public void serialize(@NotNull LatexCommandsStub latexCommandsStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeName(latexCommandsStub.getName());
        stubOutputStream.writeName(serialiseRequired(latexCommandsStub));
        stubOutputStream.writeName(serialiseOptional(latexCommandsStub));
    }

    @NotNull
    @Override
    public LatexCommandsStub deserialize(@NotNull StubInputStream stubInputStream, StubElement parent) throws IOException {
        final String name = stubInputStream.readName().toString();
        final List<String> required = deserialiseList(stubInputStream.readName().toString());
        final List<String> optional = deserialiseList(stubInputStream.readName().toString());

        return new LatexCommandsStubImpl(
                parent, this,
                name,
                required,
                optional
        );
    }

    @Override
    public void indexStub(@NotNull LatexCommandsStub latexCommandsStub, @NotNull IndexSink indexSink) {
        indexSink.occurrence(LatexCommandsIndex.Companion.key(), latexCommandsStub.getCommandToken());

        String token = latexCommandsStub.getCommandToken();
        if (Magic.Command.includes.contains(token)) {
            indexSink.occurrence(LatexIncludesIndex.Companion.key(), token);
        }

        if (Magic.Command.definitions.contains(token) || Magic.Command.redefinitions.contains(token)) {
            indexSink.occurrence(LatexDefinitionIndex.Companion.key(), token);
        }
    }

    @NotNull
    private List<String> deserialiseList(@NotNull String string) {
        return SEPERATOR.splitAsStream(string).collect(Collectors.toList());
    }

    @NotNull
    private String serialiseRequired(@NotNull LatexCommandsStub stub) {
        return String.join(SEPERATOR.pattern(), stub.getRequiredParams());
    }

    @NotNull
    private String serialiseOptional(@NotNull LatexCommandsStub stub) {
        return String.join(SEPERATOR.pattern(), stub.getOptionalParams());
    }
}
