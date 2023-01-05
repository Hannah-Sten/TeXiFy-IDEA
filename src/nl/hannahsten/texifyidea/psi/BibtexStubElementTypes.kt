package nl.hannahsten.texifyidea.psi

import com.intellij.psi.tree.IElementType

@Suppress("PropertyName")
interface BibtexStubElementTypes {

    val ENTRY: IElementType
        get() = BibtexTypes.ENTRY
}