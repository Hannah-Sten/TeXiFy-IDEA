package nl.hannahsten.texifyidea.reference

import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.util.getColorDefinitionElement
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped

/**
 * Reference from \color to the color definition.
 */
class LatexColorReference(element: LatexParameterText) : PsiReferenceBase<LatexParameterText>(element) {
    init {
        rangeInElement = ElementManipulators.getValueTextRange(element)
    }

    override fun resolve(): PsiElement? = NewSpecialCommandsIndex.getAllColorDef(myElement.containingFile.originalFile)
        // Resolve to the parameter text, so find usages works as expected
        .mapNotNull { getColorDefinitionElement(it)?.findFirstChildTyped<LatexParameterText>() }
        .firstOrNull { it.text == myElement.text }
}