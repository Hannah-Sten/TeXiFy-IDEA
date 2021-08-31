package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiFile
import com.intellij.util.ProcessingContext
import com.intellij.util.ui.ColorIcon
import nl.hannahsten.texifyidea.gutter.LatexElementColorProvider
import nl.hannahsten.texifyidea.util.Kindness
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.getCommandsInFiles
import nl.hannahsten.texifyidea.util.getRequiredArgumentValueByName
import nl.hannahsten.texifyidea.util.isColorDefinition
import nl.hannahsten.texifyidea.util.magic.ColorMagic
import java.awt.Color

object LatexXColorProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
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
        val files: MutableSet<PsiFile> = HashSet(file.referencedFileSet())
        val cmds = getCommandsInFiles(files, file)
        for (cmd in cmds) {
            if (!cmd.isColorDefinition()) {
                continue
            }

            val colorName = cmd.getRequiredArgumentValueByName("name") ?: continue
            val color = LatexElementColorProvider.findColor(colorName, file)
            val lookupElement = if (color != null) {
                LookupElementBuilder.create(colorName).withIcon(ColorIcon(12, color))
            }
            else LookupElementBuilder.create(colorName)
            result.addElement(lookupElement)
        }
        result.addLookupAdvertisement(Kindness.getKindWords())
    }
}