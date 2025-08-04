package nl.hannahsten.texifyidea.index

import nl.hannahsten.texifyidea.psi.BibtexEntry

/**
 * Index for bibtex entries.
 *
 *
 * @author Hannah Schellekens, Ernest Li
 */
class NewBibtexEntryIndexEx : LatexCompositeStubIndex<BibtexEntry>(BibtexEntry::class.java) {
    override fun getVersion(): Int {
        return 12
    }
    override fun getKey() = BibtexEntryIndexKey.KEY
}

val NewBibtexEntryIndex = NewBibtexEntryIndexEx()