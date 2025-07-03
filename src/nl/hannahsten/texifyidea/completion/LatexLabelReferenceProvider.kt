package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.LatexReferenceInsertHandler
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewLabelsIndex

object LatexLabelReferenceProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val file = parameters.originalFile
        val project = file.project
        val fileset = LatexProjectStructure.buildFilesetScope(project, file)

        NewLabelsIndex.forEachKey(project, fileset) { extractedLabel ->
            if(extractedLabel.isBlank()) return@forEachKey
            NewLabelsIndex.forEachByName(extractedLabel, project, fileset) { labelingElement ->
                val lookup = LookupElementBuilder
                    .create(extractedLabel)
                    .bold()
                    .withInsertHandler(LatexReferenceInsertHandler())
                    .withTypeText(
                        labelingElement.containingFile.name + ":" +
                            (
                                1 + StringUtil.offsetToLineNumber(
                                    labelingElement.containingFile.text,
                                    labelingElement.textOffset
                                )
                                ),
                        true
                    )
                    .withIcon(TexifyIcons.DOT_LABEL)
                result.addElement(lookup)
            }
        }
    }
}