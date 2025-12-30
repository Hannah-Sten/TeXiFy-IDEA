package nl.hannahsten.texifyidea.reference

import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import nl.hannahsten.texifyidea.psi.LatexEnvIdentifier
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

/**
 * This reference allows refactoring of environments, by letting the text in \end resolve to the text in \begin, so the \begin is viewed as a definition and the text in \end as usage.
 *
 * @author Thomas
 */
class LatexEnvironmentBeginEndReference(element: LatexEnvIdentifier, val toBegin: Boolean) : PsiReferenceBase<LatexEnvIdentifier>(element) {

    init {
        rangeInElement = ElementManipulators.getValueTextRange(element)
    }

    override fun isReferenceTo(element: PsiElement): Boolean = resolve() == element

    override fun resolve(): PsiElement? {
        // Navigate from the current text in \end, to the text in \begin
        val env = element.firstParentOfType(LatexEnvironment::class) ?: return null
        return if (toBegin) {
            env.beginCommand.envIdentifier
        }
        else {
            env.endCommand?.envIdentifier
        }
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        myElement.setName(newElementName)
        return myElement
    }
}

class LatexEnvironmentEndReference(element: LatexEnvIdentifier) : PsiReferenceBase<LatexEnvIdentifier>(element) {

    init {
        rangeInElement = ElementManipulators.getValueTextRange(element)
    }

    override fun resolve(): PsiElement? {
        // Navigate from the current text in \begin, to the text in \end
        return element.firstParentOfType(LatexEnvironment::class)?.endCommand?.envIdentifier
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        myElement.setName(newElementName)
        return myElement
    }
}