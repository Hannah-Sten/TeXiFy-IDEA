package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.util.Kindness
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.getRequiredArgumentValueByName
import nl.hannahsten.texifyidea.util.isColorDefinition
import java.util.*
import java.util.stream.Collectors

object LatexXColorProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        addDefaultColors(result)
        addCustomColors(parameters, result)
    }

    private fun addDefaultColors(result: CompletionResultSet) {
        result.addAllElements(
                Magic.Colors.defaultXcolors.map {
                    LookupElementBuilder.create(it.key)
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

            result.addElement(LookupElementBuilder.create(colorName))
        }
        result.addLookupAdvertisement(Kindness.getKindWords())
    }
}