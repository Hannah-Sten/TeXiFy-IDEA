package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.psi.BibtexEntry

/**
 * See [BibtexEntryIndex]
 *
 * @author Felix Berlakovich
 */
object BibtexEntryIndexKey {
    @JvmField
    val KEY = StubIndexKey.createIndexKey<String, BibtexEntry>("nl.hannahsten.texifyidea.bibtex.entry")
}
