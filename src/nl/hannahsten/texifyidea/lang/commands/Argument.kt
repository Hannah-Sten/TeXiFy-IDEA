package nl.hannahsten.texifyidea.lang.commands

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import nl.hannahsten.texifyidea.completion.LatexBibliographyStyleProvider
import nl.hannahsten.texifyidea.completion.LatexListTypeProvider
import nl.hannahsten.texifyidea.completion.LatexMintedTypeProvider

/**
 * @author Sten Wessel
 */
abstract class Argument @JvmOverloads protected constructor(val name: String, val type: Type = Type.NORMAL) {

    abstract override fun toString(): String

    /**
     * @author Hannah Schellekens, Sten Wessel
     */
    enum class Type(

        /**
         * Provides the autocomplete options for the argument of this type.
         * `null` for no autocomplete options.
         */
        val completionProvider: CompletionProvider<CompletionParameters>? = null
    ) {

        /**
         * Can contain any kind of argument content.
         */
        NORMAL,

        /**
         * Contains a path/reference to a file.
         */
        FILE,

        /**
         * Reference to a label.
         */
        LABEL,

        /**
         * Text contents.
         */
        TEXT,

        /**
         * Contains a bibliography style.
         */
        BIBLIOGRAPHY_STYLE(completionProvider = LatexBibliographyStyleProvider),

        /**
         * enumerate, itemize, etc.
         */
        LIST_ENVIRONMENT(completionProvider = LatexListTypeProvider),

        MINTED_FUNTIME_LAND(completionProvider = LatexMintedTypeProvider),
    }
}
