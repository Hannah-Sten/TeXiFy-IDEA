package nl.hannahsten.texifyidea.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewLabelsIndex
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.util.labels.LatexLabelUtil

/**
 * This reference works on parameter text, i.e. the actual label parameters.
 * This means that the parameter of a \ref command will resolve to the parameter of the \label command.
 *
 * This allows us to implement find usages as well.
 *
 * @author Thomas
 */
class LatexLabelParameterReference(element: LatexParameterText) : PsiReferenceBase<LatexParameterText>(element), PsiPolyVariantReference {

    private val labelName = element.text

    override fun calculateDefaultRangeInElement(): TextRange {
        val fullRange = ElementManipulators.getValueTextRange(element)
        val prefix = determinePrefix() ?: return fullRange
        val prefixLength = prefix.length
        return TextRange.create(prefixLength + fullRange.startOffset, fullRange.endOffset)
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        if (element !is LatexParameterText) {
            return false
        }
        return multiResolve(false).any { it.element == element }
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    fun determinePrefix(): String? {
        val file = myElement.containingFile
        val externalDocumentInfo = LatexProjectStructure.getFilesetDataFor(file)?.externalDocumentInfo ?: return null
        return externalDocumentInfo.firstOrNull { labelName.startsWith(it.labelPrefix) }?.labelPrefix
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return multiResolve(labelName, myElement.containingFile).toTypedArray()
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        var newName = newElementName
        val prefix = determinePrefix()
        if (prefix != null && !newName.startsWith(prefix))
            newName = prefix + newName
        myElement.setName(newName)
        return myElement
    }

    companion object {

        fun multiResolve(label: String, file: PsiFile): List<PsiElementResolveResult> {
            val result = mutableListOf<PsiElementResolveResult>()
            LatexLabelUtil.forEachLabelParamByName(label, file) {
                result.add(PsiElementResolveResult(it))
            }
            return result
        }

        fun isLabelDefined(label: String, file: PsiFile): Boolean {
            val project = file.project
            val basicSearchScope = LatexProjectStructure.getFilesetScopeFor(file, onlyTexFiles = true)
            if (NewLabelsIndex.existsByName(label, project, basicSearchScope)) return true

            LatexProjectStructure.getFilesetDataFor(file)?.externalDocumentInfo?.forEach { info ->
                if (label.startsWith(info.labelPrefix)) {
                    val labelWithoutPrefix = label.removePrefix(info.labelPrefix)
                    val scopes = info.files.map { LatexProjectStructure.getFilesetScopeFor(it, project, onlyTexFiles = true) }
                    if (NewLabelsIndex.existsByName(labelWithoutPrefix, project, GlobalSearchScope.union(scopes))) return true
                }
            }
            return false
        }
    }
}