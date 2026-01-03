package nl.hannahsten.texifyidea.psi

import nl.hannahsten.texifyidea.grammar.LatexStubFileElementType
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStubElementType
import nl.hannahsten.texifyidea.index.stub.LatexEnvironmentStubElementType
import nl.hannahsten.texifyidea.index.stub.LatexMagicCommentStubElementType

/**
 * Grammar-Kit cannot generate this file containing only stub element types, so we have to maintain it manually.
 */
@Suppress("unused")
interface LatexStubElementTypes {

    companion object {

        @JvmField
        val COMMANDS = LatexTypes.COMMANDS as LatexCommandsStubElementType

        @JvmField
        val ENVIRONMENT = LatexTypes.ENVIRONMENT as LatexEnvironmentStubElementType

        @JvmField
        val MAGIC_COMMENT = LatexTypes.MAGIC_COMMENT as LatexMagicCommentStubElementType

        @JvmField
        val FILE = LatexStubFileElementType
    }
}