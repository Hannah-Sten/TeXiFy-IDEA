package nl.hannahsten.texifyidea.psi

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic

@Suppress("FunctionName")
class LatexParserUtil : GeneratedParserUtilBase() {

    companion object {

        /**
         * @return false if the parser should parse it like normal, true if something was remapped
         */
        @JvmStatic
        fun remap_aftergroup_openbrace(builder: PsiBuilder, level: Int, rawText: Parser): Boolean {
            val currentText = builder.originalText.subSequence(
                builder.latestDoneMarker?.startOffset ?: return true,
                builder.latestDoneMarker?.endOffset ?: return true
            )

            val afterGroupIndex = builder.originalText.indexOf("\\aftergroup")
            if (afterGroupIndex != -1) {
                val shouldIgnoreAt = listOf(
                    builder.originalText.substring(0, afterGroupIndex).lastIndexOf("{"),
                    builder.originalText.indexOf("}", afterGroupIndex)
                ).filter { it != -1 }
                if (shouldIgnoreAt.isNotEmpty()) {
                    builder.setTokenTypeRemapper { source, start, end, text ->
                        if (source == LatexTypes.OPEN_BRACE && start !in shouldIgnoreAt) {
                            LatexTypes.NORMAL_TEXT_WORD
                        }
                        else {
                            source
                        }
                    }
                }
            }

            val success = rawText.parse(builder, level)
            builder.setTokenTypeRemapper(null)
            return success
        }

        /**
         * Remap tokens inside verbatim environments to raw text.
         * Requires the lexer to be in a proper state before and after the environment.
         *
         * Docs: https://github.com/JetBrains/Grammar-Kit/blob/master/HOWTO.md#23-when-nothing-helps-external-rules
         */
        @JvmStatic
        fun injection_env_content(builder: PsiBuilder, level: Int, rawText: Parser): Boolean {
            // This might be optimized by handling the tokens incrementally
            val beginText = builder.originalText.subSequence(
                builder.latestDoneMarker?.startOffset ?: return true,
                builder.latestDoneMarker?.endOffset ?: return true
            )
            val nameStart = beginText.indexOf('{') + 1
            val nameEnd = beginText.indexOf('}')
            if (nameStart >= nameEnd) return false

            val env = beginText.subSequence(nameStart, nameEnd).toString()

            if (env !in EnvironmentMagic.verbatim) return false

            val startIndex = builder.currentOffset
            // Exclude the last newline, so it will stay a whitespace,
            // otherwise the formatter (LatexSpacingRules) will insert a
            // newline too much between environment content and \end
            val endIndex = builder.originalText.indexOf("\\end{$env}", startIndex) - 1

            // If there is nothing to remap, for example because there are only newlines, return false
            if (endIndex < startIndex) return false

            // Only remap \end and whitespace tokens, other ones are already raw text by the lexer
            // This makes sure that the optional argument of a verbatim environment is not by mistake also remapped to raw text
            // \end is remapped because the lexer only knows afterwards whether it ended the environment or not, and whitespace is remapped because this allows keeping the last whitespace for the formatter
            builder.setTokenTypeRemapper { token, start, end, _ ->
                if (startIndex <= start && end <= endIndex &&
                    (token == LatexTypes.END_TOKEN || token == LatexTypes.BEGIN_TOKEN || token == LatexTypes.OPEN_BRACE || token == LatexTypes.CLOSE_BRACE || token == com.intellij.psi.TokenType.WHITE_SPACE)
                ) {
                    LatexTypes.RAW_TEXT_TOKEN
                }
                else {
                    token
                }
            }

            rawText.parse(builder, level)

            builder.setTokenTypeRemapper(null)

            return true
        }
    }
}
