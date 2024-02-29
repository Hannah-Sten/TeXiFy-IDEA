package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys

/**
 * @author Hannah Schellekens
 */
object LatexMagicCommentKeyProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val keys = DefaultMagicKeys.entries.toTypedArray()
        result.addAllElements(
            keys.map {
                LookupElementBuilder.create(it, it.displayKey)
                    .withCaseSensitivity(false)
                    .withPresentableText(it.key)
                    .bold()
                    .withIcon(TexifyIcons.KEY_USER_DEFINED)
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

            // Only add the "=" when we are not completing the "fake" magic comment.
            val postFix = if (item.lookupString == DefaultMagicKeys.FAKE.displayKey) " " else " = "
            document.insertString(offset, postFix)
            caret.moveToOffset(offset + postFix.length)
        }
    }
}