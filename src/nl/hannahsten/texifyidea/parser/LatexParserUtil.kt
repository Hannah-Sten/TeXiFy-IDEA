package nl.hannahsten.texifyidea.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.util.Magic

class LatexParserUtil : GeneratedParserUtilBase() {
    companion object {

        @JvmStatic fun injection_env_content(builder: PsiBuilder, level: Int, rawText: Parser): Boolean {
            // TODO: this must be checked for performance.
            // This might be optimized by handling the tokens incrementally
            val beginText = builder.originalText.subSequence(
                    builder.latestDoneMarker!!.startOffset,
                    builder.latestDoneMarker!!.endOffset
            )
            val nameStart = beginText.indexOf('{') + 1
            val nameEnd = beginText.indexOf('}')
            if (nameStart >= nameEnd) return false

            val env = beginText.subSequence(nameStart, nameEnd).toString()

            if (env !in Magic.Environment.verbatim) return false

            val startIndex = builder.currentOffset
            val endIndex = builder.originalText.indexOf("\\end{$env}", startIndex)


            builder.setTokenTypeRemapper { token, start, end, _ -> if (startIndex <= start && end <= endIndex) LatexTypes.RAW_TEXT_TOKEN else token }

            rawText.parse(builder, level)

            builder.setTokenTypeRemapper(null)

            return true
        }
    }
}
