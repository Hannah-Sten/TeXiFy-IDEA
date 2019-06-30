package nl.hannahsten.texifyidea.index;

import com.intellij.psi.stubs.StubIndexKey;
import nl.hannahsten.texifyidea.psi.LatexCommands;

/**
 * @author Hannah Schellekens
 */
public class IndexKeys {

    public static final StubIndexKey<String, LatexCommands> COMMANDS_KEY =
            StubIndexKey.createIndexKey("nl.hannahsten.texifyidea.commands");

    public static final StubIndexKey<String, LatexCommands> INCLUDES_KEY =
            StubIndexKey.createIndexKey("nl.hannahsten.texifyidea.includes");

    public static final StubIndexKey<String, LatexCommands> DEFINITIONS_KEY =
            StubIndexKey.createIndexKey("nl.hannahsten.texifyidea.definitions");
}
