package nl.hannahsten.texifyidea.psi

import com.intellij.psi.tree.IElementType

/**
 * Grammar-Kit cannot generate this file containing only stub element types, so we have to maintain it manually.
 */
@Suppress("PropertyName")
interface LatexStubElementTypes {

    val COMMANDS: IElementType
        get() = LatexTypes.COMMANDS
    val ENVIRONMENT: IElementType
        get() = LatexTypes.ENVIRONMENT
    val MAGIC_COMMENT: IElementType
        get() = LatexTypes.MAGIC_COMMENT
}