package nl.rubensten.texifyidea.index;

import com.intellij.psi.stubs.StubIndexKey;
import nl.rubensten.texifyidea.psi.BibtexId;

/**
 * For some reason, the key of {@link BibtexIdIndex} must be placed into a java file.
 * Maybe I'm just doing something wrong, though ¯\_(ツ)_/¯.
 *
 * @author Ruben Schellekens
 */
public class BibtexIdIndexKey {

    public static final StubIndexKey<String, BibtexId> KEY =
            StubIndexKey.createIndexKey("nl.rubensten.texifyidea.bibtex.id");
}
