package nl.hannahsten.texifyidea.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import nl.hannahsten.texifyidea.psi.BibtexContent
import nl.hannahsten.texifyidea.psi.BibtexKey
import nl.hannahsten.texifyidea.psi.BibtexTag
import nl.hannahsten.texifyidea.reference.SimpleFileReference
import nl.hannahsten.texifyidea.util.magic.FileMagic
import nl.hannahsten.texifyidea.util.parser.firstChildOfType

abstract class BibtexTagImplMixin(node: ASTNode) : BibtexTag, ASTWrapperPsiElement(node) {

    override fun getReferences(): Array<PsiReference> {
        val key = this.firstChildOfType(BibtexKey::class)?.text ?: return emptyArray()
        if (key !in FileMagic.bibtexFileKeys) return emptyArray()
        val content = this.firstChildOfType(BibtexContent::class)?.text ?: return emptyArray()

        // Mendeley contents are of the form {:full/path/to/file1.pdf:pdf;:file2.pdf:pdf}
        val contentParts = content.trimStart('{').trimEnd('}').split(";")
        val references = mutableListOf<PsiReference>()

        for (part in contentParts) {
            if (SystemInfo.isWindows) {
                references.add(SimpleFileReference(this, part))
                // The Mendeley format on Windows seems too inconsistent to work with, especially since the : has multiple meanings
            }
            else {
                // Possible split on : (maybe not necessary)
                val partSplit = part.split(":")
                if (partSplit.size > 1) {
                    // Assume we are in Mendeley format
                    val filePath = "/" + partSplit[1]
                    val textRange = TextRange.from(this.text.indexOf(partSplit[1]), filePath.length)
                    references.add(SimpleFileReference(this, filePath, textRange))
                }
                else {
                    // Assume we already have the filepath
                    val filePath = partSplit[0]
                    references.add(SimpleFileReference(this, filePath))
                }
            }
        }

        return references.toTypedArray()
    }
}