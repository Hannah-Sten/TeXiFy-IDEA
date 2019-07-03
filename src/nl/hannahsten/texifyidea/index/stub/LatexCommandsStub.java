package nl.hannahsten.texifyidea.index.stub;

import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.stubs.StubElement;
import nl.hannahsten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Hannah Schellekens
 */
public interface LatexCommandsStub extends StubElement<LatexCommands>, NamedStub<LatexCommands> {

    @NotNull
    String getCommandToken();

    @NotNull
    List<String> getRequiredParams();

    @NotNull
    List<String> getOptionalParams();
}