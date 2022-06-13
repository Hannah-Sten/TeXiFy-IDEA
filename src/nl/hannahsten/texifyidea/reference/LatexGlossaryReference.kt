package nl.hannahsten.texifyidea.reference

import com.intellij.psi.*
import com.intellij.util.containers.toArray
import nl.hannahsten.texifyidea.index.LatexGlossaryEntryIndex
import nl.hannahsten.texifyidea.lang.commands.LatexGlossariesCommand
import nl.hannahsten.texifyidea.psi.LatexParameterText

/**
 * This reference allows refactoring of glossary entries. A glossary reference command (e.g. \gls) references the label
 * parameter of a glossary entry command (e.g. \newglossaryentry).
 */
class LatexGlossaryReference(element: LatexParameterText) : PsiReferenceBase<LatexParameterText>(element),
    PsiPolyVariantReference {

    init {
        rangeInElement = ElementManipulators.getValueTextRange(element)
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        return multiResolve(false).any { it.element == element }
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val glossaryEntries = LatexGlossaryEntryIndex.getItemsInFileSet(myElement.containingFile.originalFile)
        return glossaryEntries
            .filter { LatexGlossariesCommand.extractGlossaryLabel(it) == myElement.name }
            .toSet()
            .mapNotNull {
                PsiElementResolveResult(
                    LatexGlossariesCommand.extractGlossaryLabelElement(it) ?: return@mapNotNull null
                )
            }
            .toList()
            .toArray(emptyArray())
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        myElement.setName(newElementName)
        return myElement
    }
}
