package nl.hannahsten.texifyidea.reference

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import nl.hannahsten.texifyidea.psi.BibtexKey
import nl.hannahsten.texifyidea.psi.BibtexTag
import nl.hannahsten.texifyidea.util.firstChildOfType
import nl.hannahsten.texifyidea.util.toTextRange

/**
 * When the bibtex field is (by convention) 'bibsource', then this is a reference to the pdf file which is the field value.
 * todo do mendeley or jabref generate source file entries?
 */
class BibtexTagReference(val element: BibtexTag) : PsiReferenceBase<BibtexTag>(element) {

    init { // todo range of normal text?
        rangeInElement = (0..element.textLength).toTextRange()
    }

    override fun resolve(): PsiElement? {
        val isBibsource = element.firstChildOfType(BibtexKey::class)?.text == "bibsource"
    }

}