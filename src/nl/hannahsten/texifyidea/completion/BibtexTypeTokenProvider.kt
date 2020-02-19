package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.PlatformIcons
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.hannahsten.texifyidea.completion.handlers.TokenTypeInsertHandler
import nl.hannahsten.texifyidea.lang.BibtexDefaultEntry
import nl.hannahsten.texifyidea.lang.Package

/**
 * @author Hannah Schellekens
 */
object BibtexTypeTokenProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        result.addAllElements(ContainerUtil.map2List(BibtexDefaultEntry.values()) {
            LookupElementBuilder.create(it, it.token)
                    .withPresentableText(it.token)
                    .bold()
                    .withIcon(PlatformIcons.ANNOTATION_TYPE_ICON)
                    .withTailText(" " + tags(it) + " " + packageName(it), true)
                    .withInsertHandler(TokenTypeInsertHandler)
        })
    }

    private fun tags(entry: BibtexDefaultEntry): String {
        return " {" + entry.required.joinToString { it.toString().toLowerCase() } + "}"
    }

    private fun packageName(entry: BibtexDefaultEntry): String {
        return when(val dependency = entry.dependency) {
            Package.DEFAULT -> ""
            else -> " (${dependency.name})"
        }
    }
}