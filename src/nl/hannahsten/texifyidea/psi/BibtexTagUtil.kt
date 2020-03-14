package nl.hannahsten.texifyidea.psi

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
        val filePath = if (partSplit.size > 1) {
            // Assume we are in Medeley format
            "/" + partSplit[1] // todo what does it generate on Windows
            // todo problem: now simplefilereference can't find the path in the element text
        }
        else {
            // Assume we already have the filepath
            partSplit[0]
        }
        references.add(SimpleFileReference(element, filePath))
    }

    return references.toTypedArray()
}