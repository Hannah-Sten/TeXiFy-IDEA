package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.PlatformIcons
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.completion.handlers.TokenTypeInsertHandler
import nl.hannahsten.texifyidea.lang.BibtexDefaultEntryType
import nl.hannahsten.texifyidea.lang.LatexPackage
import java.util.*

/**
 * @author Hannah Schellekens
 */
object BibtexTypeTokenProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        result.addAllElements(
            BibtexDefaultEntryType.values().map {
                LookupElementBuilder.create(it, it.token)
                    .withPresentableText(it.token)
                    .bold()
                    .withIcon(PlatformIcons.ANNOTATION_TYPE_ICON)
                    .withTailText(" " + tags(it) + " " + packageName(it), true)
                    .withInsertHandler(TokenTypeInsertHandler)
            }
        )
    }

    private fun tags(entry: BibtexDefaultEntryType): String {
        return " {" + entry.required.joinToString { it.toString().lowercase(Locale.getDefault()) } + "}"
    }

    private fun packageName(entry: BibtexDefaultEntryType): String {
        return when (val dependency = entry.dependency) {
            LatexPackage.DEFAULT -> ""
            else -> " (${dependency.name})"
        }
    }
}