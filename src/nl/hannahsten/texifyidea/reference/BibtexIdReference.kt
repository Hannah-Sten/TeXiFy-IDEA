package nl.hannahsten.texifyidea.reference

import com.intellij.psi.*
import nl.hannahsten.texifyidea.index.NewBibtexEntryIndex
import nl.hannahsten.texifyidea.psi.BibtexId
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType

/**
 * Reference to a bibtex id.
 * The LatexNormalText, for example the `knuth1990` in `\cite{knuth1990}` will resolve to the bibtex id, so the `knuth1990,` in `@Book{knuth1990,`
 */
class BibtexIdReference(element: LatexParameterText) : PsiReferenceBase<LatexParameterText>(element), PsiPolyVariantReference {

    init {
        rangeInElement = ElementManipulators.getValueTextRange(element)
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val name = myElement.name ?: return emptyArray()
        val entries = NewBibtexEntryIndex.getByNameInFileSet(name, myElement.containingFile)
        if (entries.isEmpty()) return ResolveResult.EMPTY_ARRAY
        return entries.mapNotNull { entry ->
            // Resolve to the id, similarly as why we resolve to the label text for latex labels
            entry.findFirstChildOfType(BibtexId::class)?.let { id -> PsiElementResolveResult(id) }
        }.toTypedArray()
    }

    override fun isReferenceTo(element: PsiElement): Boolean = multiResolve(false).any { it.element == element }

    override fun handleElementRename(newElementName: String): PsiElement {
        myElement.setName(newElementName)
        return myElement
    }
}