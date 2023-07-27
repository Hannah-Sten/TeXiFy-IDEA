package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.MoveToEndOfCommandHandler
import nl.hannahsten.texifyidea.util.findAvailableDocumentClasses

/**
 * @author Hannah Schellekens
 */
object LatexDocumentclassProvider : CompletionProvider<CompletionParameters>() {

    /**
     * List of all available default documentclasses.
     */
    private val DEFAULT_CLASSES = setOf(
        "article", "IEEEtran", "proc", "report", "book", "slides", "memoir", "letter", "beamer"
    )

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val project = parameters.editor.project ?: return
        val classes = DEFAULT_CLASSES + project.findAvailableDocumentClasses()
        result.addAllElements(
            classes.map { name ->
                LookupElementBuilder.create(name, name)
                    .withPresentableText(name)
                    .bold()
                    .withIcon(TexifyIcons.DOT_CLASS)
                    .withInsertHandler(MoveToEndOfCommandHandler)
            }
        )
    }
}