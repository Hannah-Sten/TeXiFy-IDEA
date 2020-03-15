package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import nl.hannahsten.texifyidea.reference.SimpleFileReference
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.firstChildOfType

fun getReferences(element: BibtexTag): Array<PsiReference> {
    val key = element.firstChildOfType(BibtexKey::class)?.text ?: return emptyArray()
    if (key !in Magic.File.bibtexFileKeys) return emptyArray()
    val content = element.firstChildOfType(BibtexContent::class)?.text ?: return emptyArray()

    // Mendeley contents are of the form {:full/path/to/file1.pdf:pdf;:file2.pdf:pdf}
    val contentParts = content.trimStart('{').trimEnd('}').split(";")
    val references = mutableListOf<PsiReference>()

    for (part in contentParts) {
        // Possible split on : (maybe not necessary)
        val partSplit = part.split(":")
        if (partSplit.size > 1) {
            // Assume we are in Medeley format
            val filePath = "/" + partSplit[1] // todo what does it generate on Windows
            val textRange = TextRange.from(element.text.indexOf(partSplit[1]), filePath.length)
            references.add(SimpleFileReference(element, filePath, textRange))
        }
        else {
            // Assume we already have the filepath
            val filePath = partSplit[0]
            references.add(SimpleFileReference(element, filePath))
        }
    }

    return references.toTypedArray()
}