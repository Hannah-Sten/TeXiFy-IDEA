package nl.rubensten.texifyidea.index;

import com.intellij.psi.stubs.StubIndexKey;
import nl.rubensten.texifyidea.psi.LatexCommands;

/**
 * @author Ruben Schellekens
 */
public class IndexKeys {

    public static final StubIndexKey<String, LatexCommands> COMMANDS_KEY =
            StubIndexKey.createIndexKey("nl.rubensten.texifyidea.commands");

    public static final StubIndexKey<String, LatexCommands> INCLUDES_KEY =
            StubIndexKey.createIndexKey("nl.rubensten.texifyidea.includes");

    public static final StubIndexKey<String, LatexCommands> DEFINITIONS_KEY =
            StubIndexKey.createIndexKey("nl.rubensten.texifyidea.definitions");
}
