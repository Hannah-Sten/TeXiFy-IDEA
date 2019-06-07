package nl.rubensten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.ALL_TEXIFY_INSPECTIONS

/**
 * @author Ruben Schellekens
 */
object LatexInspectionIdProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val file = parameters.originalFile
        val inspectionIds = InsightGroup.byFileType(file.fileType)
                .flatMap { ALL_TEXIFY_INSPECTIONS[it] ?: emptyList() }

        result.addAllElements(ContainerUtil.map2List(inspectionIds) {
            LookupElementBuilder.create(it, it)
                    .withPresentableText(it)
                    .bold()
                    .withIcon(AllIcons.General.InspectionsEye)
        })
    }
}