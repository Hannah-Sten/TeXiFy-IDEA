package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.CompositeHandler
import nl.hannahsten.texifyidea.completion.handlers.MoveToEndOfCommandHandler
import nl.hannahsten.texifyidea.completion.handlers.RefreshFilesetHandler
import nl.hannahsten.texifyidea.util.PackageUtils

/**
 * @author Hannah Schellekens
 */
object LatexPackageNameProvider : LatexContextAgnosticCompletionProvider() {

    override fun addCompletions(parameters: CompletionParameters, result: CompletionResultSet) {
        val packageNames = PackageUtils.CTAN_PACKAGE_NAMES

        result.addAllElements(
            packageNames.map { name ->
                LookupElementBuilder.create(name, name)
                    .withPresentableText(name)
                    .bold()
                    .withIcon(TexifyIcons.STYLE_FILE)
                    .withInsertHandler(
                        CompositeHandler(
                            MoveToEndOfCommandHandler,
                            RefreshFilesetHandler
                        )
                    )
            }
        )
    }
}