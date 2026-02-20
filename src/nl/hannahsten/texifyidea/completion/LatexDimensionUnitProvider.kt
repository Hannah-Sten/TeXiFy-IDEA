package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder

object LatexDimensionUnitProvider : LatexContextAgnosticCompletionProvider() {

    private val DIMENSION_UNITS = listOf("pt", "pc", "bp", "in", "cm", "mm", "dd", "cc", "sp", "em", "ex", "mu")
    private val UNIT_DESCRIPTIONS = mapOf(
        "pt" to "point (1/72.27 in)",
        "pc" to "pica (12 pt)",
        "bp" to "big point (1/72 in)",
        "in" to "inch",
        "cm" to "centimeter",
        "mm" to "millimeter",
        "dd" to "didot point",
        "cc" to "cicero (12 dd)",
        "sp" to "scaled point (1/65536 pt)",
        "em" to "font-dependent em width",
        "ex" to "font-dependent x-height",
        "mu" to "math unit (1/18 em)"
    )
    private val DIMENSION_PREFIX_PATTERN = Regex("""^([+-]?(?:\d+(?:\.\d*)?|\.\d+))([a-zA-Z]*)$""")

    override fun addCompletions(parameters: CompletionParameters, result: CompletionResultSet) {
        result.restartCompletionOnAnyPrefixChange()

        val prefix = result.prefixMatcher.prefix.trim()
        val match = DIMENSION_PREFIX_PATTERN.matchEntire(prefix) ?: return

        val numberPart = match.groupValues[1]
        val unitPrefix = match.groupValues[2].lowercase()
        val candidates = DIMENSION_UNITS.filter { it.startsWith(unitPrefix) }

        if (candidates.isEmpty()) {
            return
        }

        result.addAllElements(
            candidates.map { unit ->
                LookupElementBuilder.create("$numberPart$unit")
                    .withPresentableText(unit)
                    .withTypeText(UNIT_DESCRIPTIONS[unit], true)
                    .bold()
            }
        )
    }
}
