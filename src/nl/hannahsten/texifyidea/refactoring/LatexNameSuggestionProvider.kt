package nl.hannahsten.texifyidea.refactoring

import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.SuggestedNameInfo
import com.intellij.refactoring.rename.NameSuggestionProvider
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

class LatexNameSuggestionProvider : NameSuggestionProvider {

    override fun getSuggestedNames(element: PsiElement, nameSuggestionContext: PsiElement?, result: MutableSet<String>): SuggestedNameInfo? {
        if (element.firstParentOfType(LatexBeginCommand::class) != null) {
            result.addAll(DefaultEnvironment.values().map { it.environmentName })
        }

        return null
    }
}