package nl.hannahsten.texifyidea.reference

import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.util.psi.firstChildOfType
import nl.hannahsten.texifyidea.util.psi.firstParentOfType

/**
 * This reference allows refactoring of environments, by letting the text in \end resolve to the text in \begin, so the \begin is viewed as a definition and the text in \end as usage.
 *
 * @author Thomas
 */
class LatexEnvironmentReference(element: LatexParameterText) : PsiReferenceBase<LatexParameterText>(element) {

    init {
        rangeInElement = ElementManipulators.getValueTextRange(element)
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        return resolve() == element
    }

    override fun resolve(): PsiElement? {
        // Navigate from the current text in \end, to the text in \begin
        return element.firstParentOfType(LatexEnvironment::class)
            ?.firstChildOfType(LatexBeginCommand::class)
            ?.firstChildOfType(LatexParameterText::class)
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        myElement.setName(newElementName)
        return myElement
    }
}