package nl.hannahsten.texifyidea.reference

import com.intellij.psi.*
import com.intellij.util.containers.toArray
import nl.hannahsten.texifyidea.index.BibtexEntryIndex
import nl.hannahsten.texifyidea.psi.BibtexId
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.util.labels.extractLabelName
import nl.hannahsten.texifyidea.util.firstChildOfType

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
        return BibtexEntryIndex.getIndexedEntriesInFileSet(myElement.containingFile)
            .filter { it.extractLabelName() == myElement.name }
            .mapNotNull {
                // Resolve to the id, similarly as why we resolve to the label text for latex labels
                val id = it.firstChildOfType(BibtexId::class) ?: return@mapNotNull null
                PsiElementResolveResult(id)
            }
            .toList()
            .toArray(emptyArray())
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        return multiResolve(false).any { it.element == element }
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        myElement.setName(newElementName)
        return myElement
    }
}