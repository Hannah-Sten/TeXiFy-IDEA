package nl.rubensten.texifyidea.reference

import com.intellij.psi.*
import nl.rubensten.texifyidea.psi.BibtexDefinedString
import nl.rubensten.texifyidea.psi.BibtexEntry
import nl.rubensten.texifyidea.util.childrenOfType
import nl.rubensten.texifyidea.util.tags
import nl.rubensten.texifyidea.util.toTextRange
import nl.rubensten.texifyidea.util.tokenName

/**
 * @author Ruben Schellekens
 */
open class BibtexStringReference(
        val string: BibtexDefinedString
) : PsiReferenceBase<BibtexDefinedString>(string), PsiPolyVariantReference {

    init {
        rangeInElement = (0..string.textLength).toTextRange()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return string.containingFile.childrenOfType(BibtexEntry::class)
                .filter { it.tokenName()?.toLowerCase() == "string" }
                .map { it.tags() }
                .filter { !it.isEmpty() }
                .map { it.first().key }
                .filter { it.text == string.text }
                .map { PsiElementResolveResult(it) }
                .toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val results = multiResolve(false)
        return if (results.size == 1) results[0].element else null
    }

    override fun getVariants() = emptyArray<Any>()
}