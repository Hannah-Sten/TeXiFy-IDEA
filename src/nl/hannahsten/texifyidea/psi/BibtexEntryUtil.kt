package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.paths.WebReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.util.containers.toArray
import nl.hannahsten.texifyidea.util.evaluate
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * Get all the references from a BibTeX entry.
 */
fun getReferences(element: BibtexEntry): Array<PsiReference> {
    val urls = CommandMagic.bibUrls
        .map { element.getTagContent(it) }
        .filter { it.isNotBlank() }

    if (urls.isNotEmpty()) {
        // We cannot simply return urls.map { WebReference(element, it) } because
        // then IJ cannot find the range of the url in the entry (and thus it
        // doesn't know which text to underline). So we have to return a list of
        // WebReference(element, text range) instead.
        val contentText = element.entryContent?.text ?: return emptyArray()
        val rangesInParent = urls.mapNotNull {
            if (it !in contentText) {
                null
            }
            else {
                TextRange.from(contentText.indexOf(it), it.length)
            }
        }

        return rangesInParent.map {
            WebReference(element, it.shiftRight(element.entryContent!!.textOffset - element.textOffset))
        }.toArray(emptyArray())
    }

    return emptyArray()
}

/**
 * Get the content of a tag in a given BibTeX entry.
 */
fun getTagContent(element: BibtexEntry, tagName: String): String {
    val entryContent = element.entryContent ?: return ""

    entryContent.tagList.forEach {
        if (tagName.equals(it.key.text, ignoreCase = true)) {
            val text = it.content?.evaluate() ?: return ""

            // Deal with braced strings.
            return if (text.first() == '{' && text.last() == '}') {
                text.substring(1, text.length - 1)
            }
            else text
        }
    }

    return ""
}