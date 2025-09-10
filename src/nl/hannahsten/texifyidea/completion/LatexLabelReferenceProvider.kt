package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PlainPrefixMatcher
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.LatexReferenceInsertHandler
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.FilesetData
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewLabelsIndex
import nl.hannahsten.texifyidea.index.restrictedByFileTypes

object LatexLabelReferenceProvider : LatexContextAgnosticCompletionProvider() {

    private fun addCompletionsUnderFileset(
        parameters: CompletionParameters, result: CompletionResultSet,
        scope: GlobalSearchScope, prefix: String = ""
    ) {
        val file = parameters.originalFile
        val project = file.project
        NewLabelsIndex.forEachKey(project, scope) { extractedLabel ->
            if (extractedLabel.isBlank()) return@forEachKey
            NewLabelsIndex.forEachByName(extractedLabel, project, scope) { labelingElement ->
                val lookup = LookupElementBuilder
                    .create(prefix + extractedLabel)
                    .bold()
                    .withInsertHandler(LatexReferenceInsertHandler())
                    .withTypeText(
                        labelingElement.containingFile.name + ":" +
                            (1 + labelingElement.containingFile.fileDocument.getLineNumber(labelingElement.textOffset)),
                        true
                    )
                    .withIcon(TexifyIcons.DOT_LABEL)

                result.addElement(lookup)
            }
        }
    }

    private fun addExternalDocumentCompletions(
        parameters: CompletionParameters, result: CompletionResultSet,
        filesetData: FilesetData
    ) {
        val project = parameters.originalFile.project
        filesetData.externalDocumentInfo.forEach { info ->
            val prefix = info.labelPrefix
            val scopes = info.files.map { LatexProjectStructure.getFilesetScopeFor(it, project, onlyTexFiles = true, onlyProjectFiles = true) }
            val scope = GlobalSearchScope.union(scopes)
            addCompletionsUnderFileset(parameters, result, scope, prefix)
        }
    }

    override fun addCompletions(parameters: CompletionParameters, result: CompletionResultSet) {
        val file = parameters.originalFile
        val filesetData = LatexProjectStructure.getFilesetDataFor(file) ?: return

        val rs = result.withPrefixMatcher(PlainPrefixMatcher(result.prefixMatcher.prefix, false))
        // the label names are not necessarily camel case, so we use a plain prefix matcher
        addCompletionsUnderFileset(parameters, rs, filesetData.filesetScope.restrictedByFileTypes(LatexFileType))
        addExternalDocumentCompletions(parameters, rs, filesetData)
    }
}