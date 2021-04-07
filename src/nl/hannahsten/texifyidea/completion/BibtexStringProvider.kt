package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.lang.Described
import nl.hannahsten.texifyidea.psi.BibtexComment
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.BibtexTag
import nl.hannahsten.texifyidea.util.*

/**
 * I think this provides autocompletion for strings defined with @string{..} commands.
 *
 * @author Hannah Schellekens
 */
object BibtexStringProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val psiFile = parameters.originalFile
        val strings: List<Triple<String, String, BibtexEntry>> = psiFile.childrenOfType(BibtexEntry::class).asSequence()
            .filter { it.tokenType() == "@string" }
            .mapNotNull {
                val tag = it.firstChildOfType(BibtexTag::class) ?: return@mapNotNull null
                val key = tag.key
                val content = tag.content ?: return@mapNotNull null
                Triple(key.text, content.text, it)
            }
            .toList()

        result.addAllElements(
            ContainerUtil.map2List(strings) {
                LookupElementBuilder.create(StringDescription(it!!.third), it.first)
                    .withPresentableText(it.first)
                    .bold()
                    .withTypeText(it.second, true)
                    .withIcon(TexifyIcons.STRING)
            }
        )
    }

    class StringDescription(entry: BibtexEntry?) : Described {

        override val description: String

        init {
            val previous = entry?.previousSiblingIgnoreWhitespace()
            val comment = previous?.lastChildOfType(BibtexComment::class) ?: entry?.previousSiblingIgnoreWhitespace()
            this.description = comment?.text?.replace(Regex("^%\\s?"), "") ?: "User defined string."
        }
    }
}