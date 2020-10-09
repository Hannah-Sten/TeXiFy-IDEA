package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.PlatformIcons
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys

/**
 * @author Hannah Schellekens
 */
object LatexMagicCommentKeyProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val keys = DefaultMagicKeys.values()
        result.addAllElements(
            ContainerUtil.map2List(keys) {
                LookupElementBuilder.create(it, it.displayKey)
                    .withCaseSensitivity(false)
                    .withPresentableText(it.key)
                    .bold()
                    .withIcon(PlatformIcons.PROTECTED_ICON)
                    .withInsertHandler(InsertHandler)
            }
        )
    }

    /**
     * @author Hannah Schellekens
     */
    object InsertHandler : com.intellij.codeInsight.completion.InsertHandler<LookupElement> {

        override fun handleInsert(context: InsertionContext, item: LookupElement) {
            val editor = context.editor
            val document = editor.document
            val caret = editor.caretModel
            val offset = caret.offset

            document.insertString(offset, " = ")
            caret.moveToOffset(offset + 3)
        }
    }
}