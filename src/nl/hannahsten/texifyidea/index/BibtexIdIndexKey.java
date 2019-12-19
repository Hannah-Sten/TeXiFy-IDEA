package nl.hannahsten.texifyidea.index;

import com.intellij.psi.stubs.StubIndexKey;
import nl.hannahsten.texifyidea.psi.BibtexId;

/**
 * For some reason, the key of {@link BibtexEntryIndex} must be placed into a java file.
 * Maybe I'm just doing something wrong, though ¯\_(ツ)_/¯.
 *
 * @author Hannah Schellekens
 */
public class BibtexIdIndexKey {

    public static final StubIndexKey<String, BibtexId> KEY =
            StubIndexKey.createIndexKey("nl.hannahsten.texifyidea.bibtex.id");
}


