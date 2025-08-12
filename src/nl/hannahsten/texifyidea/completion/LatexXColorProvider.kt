package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ui.ColorIcon
import nl.hannahsten.texifyidea.gutter.LatexElementColorProvider
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.util.Kindness
import nl.hannahsten.texifyidea.util.magic.ColorMagic
import nl.hannahsten.texifyidea.util.parser.getRequiredArgumentValueByName
import java.awt.Color

object LatexXColorProvider : LatexContextAgnosticCompletionProvider() {

    override fun addCompletions(parameters: CompletionParameters, result: CompletionResultSet) {
        addDefaultColors(result)
        addCustomColors(parameters, result)
    }

    private fun addDefaultColors(result: CompletionResultSet) {
        result.addAllElements(
            ColorMagic.defaultXcolors.map {
                LookupElementBuilder.create(it.key)
                    .withIcon(ColorIcon(12, Color(it.value)))
            }
        )
    }

    private fun addCustomColors(parameters: CompletionParameters, result: CompletionResultSet) {
        val file = parameters.originalFile
        val colorDefinitions = NewCommandsIndex.getByNames(ColorMagic.colorDefinitions, file.project)
        for (cmd in colorDefinitions) {
            val colorName = cmd.getRequiredArgumentValueByName("name") ?: continue
            val color = LatexElementColorProvider().findColor(colorName, file)
            val lookupElement = if (color != null) {
                LookupElementBuilder.create(colorName).withIcon(ColorIcon(12, color))
            }
            else LookupElementBuilder.create(colorName)
            result.addElement(lookupElement)
        }
        result.addLookupAdvertisement(Kindness.getKindWords())
    }
}