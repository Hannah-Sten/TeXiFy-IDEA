package nl.hannahsten.texifyidea.psi

import nl.hannahsten.texifyidea.index.stub.BibtexEntryStubElementType

/**
 * See LatexStubElementTypes
 */
interface BibtexStubElementTypes {
    companion object {
        @JvmField
        val ENTRY = BibtexTypes.ENTRY as BibtexEntryStubElementType
    }
}