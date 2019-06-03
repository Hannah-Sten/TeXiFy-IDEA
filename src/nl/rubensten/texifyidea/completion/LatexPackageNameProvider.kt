package nl.rubensten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.completion.handlers.MoveToEndOfCommandHandler
import nl.rubensten.texifyidea.index.LatexDefinitionIndex
import nl.rubensten.texifyidea.util.PackageUtils
import nl.rubensten.texifyidea.util.projectSearchScope
import nl.rubensten.texifyidea.util.requiredParameter

/**
 * @author Ruben Schellekens
 */
object LatexPackageNameProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val packageNames = PackageUtils.CTAN_PACKAGE_NAMES.toMutableSet()
        val project = parameters.originalFile.project
        val customPackages = LatexDefinitionIndex.getCommandsByName("\\ProvidesPackage", project, project.projectSearchScope)
        packageNames.addAll(customPackages.mapNotNull { it.requiredParameter(0)?.trim() })

        result.addAllElements(ContainerUtil.map2List(packageNames) { name ->
            LookupElementBuilder.create(name, name)
                    .withPresentableText(name)
                    .bold()
                    .withIcon(TexifyIcons.STYLE_FILE)
                    .withInsertHandler(MoveToEndOfCommandHandler)
        })
    }
}