package nl.rubensten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.BibtexEntry
import nl.rubensten.texifyidea.psi.BibtexTag
import nl.rubensten.texifyidea.util.childrenOfType
import nl.rubensten.texifyidea.util.firstChildOfType
import nl.rubensten.texifyidea.util.tokenType

/**
 * @author Ruben Schellekens
 */
object BibtexStringProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet) {
        val psiFile = parameters.originalFile
        val strings: List<Pair<String, String>?> = psiFile.childrenOfType(BibtexEntry::class)
                .filter { it.tokenType() == "@string" }
                .map {
                    val tag = it.firstChildOfType(BibtexTag::class) ?: return@map null
                    val key = tag.key
                    val content = tag.content
                    Pair(key.text, content.text)
                }

        result.addAllElements(ContainerUtil.map2List(strings, {
            LookupElementBuilder.create(it!!.first, it.first)
                    .withPresentableText(it.first)
                    .bold()
                    .withTypeText(it.second, true)
                    .withIcon(TexifyIcons.STRING)
        }))
    }
}