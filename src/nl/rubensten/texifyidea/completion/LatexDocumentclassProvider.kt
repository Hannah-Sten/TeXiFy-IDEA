package nl.rubensten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.completion.handlers.MoveToEndOfCommandHandler
import nl.rubensten.texifyidea.util.findAvailableDocumentClasses

/**
 * @author Ruben Schellekens
 */
object LatexDocumentclassProvider : CompletionProvider<CompletionParameters>() {

    /**
     * List of all available default documentclasses.
     */
    private val DEFAULT_CLASSES = setOf(
            "article", "IEEEtran", "proc", "report", "book", "slides", "memoir", "letter", "beamer"
    )

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet) {
        val project = parameters.editor.project ?: return
        val classes = DEFAULT_CLASSES + project.findAvailableDocumentClasses()
        result.addAllElements(ContainerUtil.map2List(classes) { name ->
            LookupElementBuilder.create(name, name)
                    .withPresentableText(name)
                    .bold()
                    .withIcon(TexifyIcons.DOT_CLASS)
                    .withInsertHandler(MoveToEndOfCommandHandler)
        })
    }
}