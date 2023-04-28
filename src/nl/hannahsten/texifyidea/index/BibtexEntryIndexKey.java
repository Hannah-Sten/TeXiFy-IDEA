package nl.hannahsten.texifyidea.index;

import com.intellij.psi.stubs.StubIndexKey;
import nl.hannahsten.texifyidea.psi.BibtexEntry;

/**
 * According to the original implementation in BibtexidIndexKey:
 * For some reason, the key of {@link BibtexEntryIndex} must be placed into a java file.
 * Maybe I'm just doing something wrong, though ¯\_(ツ)_/¯.
 * todo try @JvmStatic
 *
 * @author Felix Berlakovich
 */
public class BibtexEntryIndexKey {

    public static final StubIndexKey<String, BibtexEntry> KEY =
            StubIndexKey.createIndexKey("nl.hannahsten.texifyidea.bibtex.entry");
}
