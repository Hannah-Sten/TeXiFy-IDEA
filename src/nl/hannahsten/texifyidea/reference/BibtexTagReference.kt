package nl.hannahsten.texifyidea.reference

import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceBase
import nl.hannahsten.texifyidea.psi.BibtexContent
import nl.hannahsten.texifyidea.psi.BibtexKey
import nl.hannahsten.texifyidea.psi.BibtexTag
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.firstChildOfType
import nl.hannahsten.texifyidea.util.toTextRange

/**
 * When the bibtex field is (by convention) 'bibsource', then this is a reference to the pdf file which is the field value.
 * A BibtexTag is the "key=value" part.
 */
class BibtexTagReference(val element: BibtexTag) : PsiReferenceBase<BibtexTag>(element) {

    init {
        val contentRange = element.firstChildOfType(BibtexContent::class)?.textRangeInParent
        rangeInElement = contentRange?.cutOut((1..contentRange.length - 2).toTextRange()) ?: (0..element.textLength).toTextRange()
    }

    override fun resolve(): PsiFile? {
        if (element.firstChildOfType(BibtexKey::class)?.text != "bibsource") return null
        val filePath = element.firstChildOfType(BibtexContent::class)
                ?.text
                ?.trimStart('{')
                ?.trimEnd('}') ?: return null

        return element.containingFile
                .containingDirectory
                .virtualFile
                .findFileByRelativePath(filePath)
                ?.psiFile(element.project)
    }

}