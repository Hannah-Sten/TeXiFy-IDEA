package nl.rubensten.texifyidea.index.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class LatexCommandsStubImpl extends NamedStubBase<LatexCommands> implements LatexCommandsStub {

    private final String commandToken;
    private final List<String> requiredParams;
    private final List<String> optionalParams;

    protected LatexCommandsStubImpl(StubElement parent, IStubElementType elementType, String commandToken,
                                    List<String> requiredParams, List<String> optionalParams) {
        super(parent, elementType, commandToken);

        this.commandToken = commandToken;
        this.requiredParams = requiredParams;
        this.optionalParams = optionalParams;
    }

    @Override
    public String getName() {
        return getCommandToken();
    }

    @NotNull
    public String getCommandToken() {
        return commandToken;
    }

    @NotNull
    public List<String> getRequiredParams() {
        return Collections.unmodifiableList(requiredParams);
    }

    @NotNull
    public List<String> getOptionalParams() {
        return Collections.unmodifiableList(optionalParams);
    }

    @Override
    public String toString() {
        return "LatexCommandsStubImpl{" + "commandToken='" + commandToken + '\'' +
                ", requiredParams=" + requiredParams +
                ", optionalParams=" + optionalParams +
                '}';
    }
}
