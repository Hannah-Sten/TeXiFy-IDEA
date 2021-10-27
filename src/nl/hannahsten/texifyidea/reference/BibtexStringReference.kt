package nl.hannahsten.texifyidea.reference

import com.intellij.psi.*
import nl.hannahsten.texifyidea.psi.BibtexDefinedString
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.tags
import nl.hannahsten.texifyidea.util.toTextRange
import nl.hannahsten.texifyidea.util.tokenName
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class BibtexStringReference(
    val string: BibtexDefinedString
) : PsiReferenceBase<BibtexDefinedString>(string), PsiPolyVariantReference {

    init {
        rangeInElement = (0..string.textLength).toTextRange()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return string.containingFile.childrenOfType(BibtexEntry::class).asSequence()
            .filter { it.tokenName()?.toLowerCase() == "string" }
            .map { it.tags() }
            .filter { !it.isEmpty() }
            .map { it.first().key }
            .filter { it.text == string.text }
            .map { PsiElementResolveResult(it) }
            .toList()
            .toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val results = multiResolve(false)
        return if (results.size == 1) results[0].element else null
    }

    override fun getVariants() = emptyArray<Any>()
}