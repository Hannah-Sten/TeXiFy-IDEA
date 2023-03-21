package nl.hannahsten.texifyidea.psi

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic

@Suppress("FunctionName")
class LatexParserUtil : GeneratedParserUtilBase() {

    companion object {

        /**
         * Remap tokens inside verbatim environments to raw text.
         * Requires the lexer to be in a proper state before and after the environment.
         */
        @JvmStatic fun injection_env_content(builder: PsiBuilder, level: Int, rawText: Parser): Boolean {
            // This might be optimized by handling the tokens incrementally
            val beginText = builder.originalText.subSequence(
                builder.latestDoneMarker?.startOffset ?: return true,
                builder.latestDoneMarker?.endOffset ?: return true
            )
            val nameStart = beginText.indexOf('{') + 1
            val nameEnd = beginText.indexOf('}')
            if (nameStart >= nameEnd) return false

            val env = beginText.subSequence(nameStart, nameEnd).toString()

            if (!EnvironmentMagic.isProbablyVerbatim(env)) return false

            val startIndex = builder.currentOffset
            // Exclude the last newline, so it will stay a whitespace,
            // otherwise the formatter (LatexSpacingRules) will insert a
            // newline too much between environment content and \end
            val endIndex = builder.originalText.indexOf("\\end{$env}", startIndex) - 1

            // If there is nothing to remap, for example because there are only newlines, return false
            if (endIndex < startIndex) return false

            // Only remap \end and whitespace tokens, other ones are already raw text by the lexer
            // This makes sure the the optional argument of a verbatim environment is not by mistake also remapped to raw text
            // \end is remapped because the lexer only knows afterwards whether it ended the environment or not, and whitespace is remapped because this allows keeping the last whitespace for the formatter
            builder.setTokenTypeRemapper { token, start, end, _ ->
                if (startIndex <= start && end <= endIndex &&
                    (token == LatexTypes.END_TOKEN || token == LatexTypes.BEGIN_TOKEN || token == LatexTypes.OPEN_BRACE || token == LatexTypes.OPEN_BRACE || token == com.intellij.psi.TokenType.WHITE_SPACE)
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
