package nl.hannahsten.texifyidea.index

import nl.hannahsten.texifyidea.psi.BibtexEntry

/**
 * Index for bibtex entries.
 *
 *
 * @author Hannah Schellekens, Ernest Li
 */
class NewBibtexEntryIndexEx : NewLatexCompositeStubIndex<BibtexEntry>(BibtexEntry::class.java) {
    override fun getVersion(): Int {
        return 11
    }
    override fun getKey() = BibtexEntryIndexKey.KEY
}

val NewBibtexEntryIndex = NewBibtexEntryIndexEx()