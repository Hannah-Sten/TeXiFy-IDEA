package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import com.intellij.util.ui.ColorIcon
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.gutter.LatexElementColorProvider
import nl.hannahsten.texifyidea.util.Kindness
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.getRequiredArgumentValueByName
import nl.hannahsten.texifyidea.util.isColorDefinition
import nl.hannahsten.texifyidea.util.magic.ColorMagic
import java.awt.Color
import java.util.*
import java.util.stream.Collectors

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
        val project = parameters.editor.project ?: return
        val file = parameters.originalFile
        val files: MutableSet<PsiFile> = HashSet(file.referencedFileSet())
        val searchFiles = files.stream()
            .map { obj: PsiFile -> obj.virtualFile }
            .collect(Collectors.toSet())
        searchFiles.add(file.virtualFile)
        val scope = GlobalSearchScope.filesScope(project, searchFiles)
        val cmds = LatexCommandsIndex.getItems(project, scope)
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