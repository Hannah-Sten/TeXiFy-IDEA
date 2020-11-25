package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.ALL_TEXIFY_INSPECTIONS

/**
 * @author Hannah Schellekens
 */
object LatexInspectionIdProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val file = parameters.originalFile
        val inspectionIds = InsightGroup.byFileType(file.fileType)
            .flatMap { ALL_TEXIFY_INSPECTIONS[it] ?: emptyList() }

        // Because it was too difficult to parse elements of the magic comment separately,
        // the default prefix matcher takes the complete comment as prefix (including %! suppress =)
        // so we override the prefix to remove that manually.
        val prefix = """suppress\s*=\s*(.*)IntellijIdeaRulezzz""".toRegex()
            .find(parameters.position.text)?.groupValues?.getOrNull(1)
        if (prefix != null) {
            result.withPrefixMatcher(prefix)
        }
        else {
            result
        }.addAllElements(
            ContainerUtil.map2List(inspectionIds) {
                LookupElementBuilder.create(it, it)
                    .withPresentableText(it)
                    .bold()
                    .withIcon(AllIcons.General.InspectionsEye)
            }
        )
    }
}