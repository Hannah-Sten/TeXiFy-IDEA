package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import javax.swing.Icon

class LatexMagicCommentValueProvider(private val prefixRegex: Regex, val values: Set<String>, val icon: Icon? = null) :
    CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        // Because it was too difficult to parse elements of the magic comment separately,
        // the default prefix matcher takes the complete comment as prefix (including the key, e.g., suppress =)
        // so we override the prefix to remove that manually.
        val prefix = prefixRegex.replaceFirst(result.prefixMatcher.prefix, "")

        result.withPrefixMatcher(prefix)
            .addAllElements(
                values.map {
                    LookupElementBuilder.create(it)
                        .bold()
                        .withIcon(icon)
                }
            )
    }
}