package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PlainPrefixMatcher
import com.intellij.codeInsight.lookup.LookupElementBuilder
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.LatexReferenceInsertHandler
import nl.hannahsten.texifyidea.util.labels.LatexLabelUtil

object LatexLabelReferenceProvider : LatexContextAgnosticCompletionProvider() {

    override fun addCompletions(parameters: CompletionParameters, result: CompletionResultSet) {
        val file = parameters.originalFile
        val rs = result.withPrefixMatcher(PlainPrefixMatcher(result.prefixMatcher.prefix, false))
        // the label names are not necessarily camel case, so we use a plain prefix matcher

        LatexLabelUtil.processAllLabelsInFileSet(file, withExternal = true) { label, element ->
            val containingFile = element.containingFile
            val lookup = LookupElementBuilder
                .create(label)
                .bold()
                .withInsertHandler(LatexReferenceInsertHandler())
                .withTypeText(
                    containingFile.name + ":" +
                        (1 + containingFile.fileDocument.getLineNumber(element.textOffset)),
                    true
                )
                .withIcon(TexifyIcons.DOT_LABEL)

            rs.addElement(lookup)
        }
    }
}