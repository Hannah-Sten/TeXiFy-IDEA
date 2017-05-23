package nl.rubensten.texifyidea.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ruben Schellekens
 */
public class LatexCommandsIndex extends StringStubIndexExtension<LatexCommands> {

    public static final StubIndexKey<String, LatexCommands> KEY =
            StubIndexKey.createIndexKey("nl.rubensten.texifyidea.commands");

    @NotNull
    @Override
    public StubIndexKey<String, LatexCommands> getKey() {
        return KEY;
    }
}
