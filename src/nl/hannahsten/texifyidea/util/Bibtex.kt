package nl.hannahsten.texifyidea.util

import com.intellij.openapi.application.runReadAction
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.parser.childrenOfType
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType
import java.util.*

/**
 * Get the lowercase token type (including `@`) of the BibTeX entry (e.g. `@article`).
 */
fun BibtexEntry.tokenType(): String = type.text.lowercase(Locale.getDefault())

/**
 * Get the token type (excluding `@`) of the BibTeX entry (e.g. `article`).
 */
fun BibtexEntry.tokenName(): String = tokenType().substring(1)

/**
 * Get all the tags in the entry.
 */
fun BibtexEntry.tags(): Collection<BibtexTag> = childrenOfType(BibtexTag::class)

/**
 * Get all the key objects in the entry.
 */
fun BibtexEntry.keys(): Collection<BibtexKey> = childrenOfType(BibtexKey::class)

/**
 * Get the first key in the entry, or `null` when there are no keys in the entry.
 */
fun BibtexEntry.firstKey() = keys().firstOrNull()

/**
 * Get all the names of all entry's keys.
 */
fun BibtexEntry.keyNames(): Collection<String> = keys().map { it.text }

/**
 * Checks if the entry is a @string.
 */
fun BibtexEntry.isString() = tokenName().lowercase(Locale.getDefault()) == "string"

/**
 * Checks if the entry is a @preamble.
 */
fun BibtexEntry.isPreamble() = tokenName().lowercase(Locale.getDefault()) == "preamble"

/**
 * Get the key of the BibTeX tag.
 */
fun BibtexTag.key(): BibtexKey? = findFirstChildOfType(BibtexKey::class)

/**
 * Get the key of the BibTeX tag in string form.
 */
fun BibtexTag.keyName(): String? = key()?.text

/**
 * Get the content of the BibTeX tag.
 */
fun BibtexTag.content(): BibtexContent? = findFirstChildOfType(BibtexContent::class)

/**
 * Get the name/identifier of the bibtex id.
 */
fun BibtexId.idName(): String = text.substringEnd(1)

/**
 * Evaluates the string contents and returns a nicely concatenated version.
 *
 * E.g. `{Bambi} # space # "Broodje"` with `@string{space=" "}` will become `Bambi Broodje`.
 */
fun BibtexContent.evaluate(): String {
    var result = ""

    runReadAction {
        for (string in childrenOfType(BibtexString::class)) {
            val braced = string.bracedString
            val quoted = string.quotedString
            val bracedVerbatim = string.bracedVerbatim
            val quotedVerbatim = string.quotedVerbatim
            val defined = string.definedString

            if (braced != null) {
                result += braced.evaluate()
            }
            else if (quoted != null) {
                result += quoted.evaluate()
            }
            else if (defined != null) {
                result += defined.evaluate()
            }
            else if (bracedVerbatim != null) {
                result += bracedVerbatim.evaluate()
            }
            else if (quotedVerbatim != null) {
                result += quotedVerbatim.evaluate()
            }
        }

        if (result.isEmpty()) {
            result += text
        }
    }

    return result
}

/**
 * Returns the value of the defined string.
 *
 * When no value could be found, `$IDENTIFIER` is returned where `IDENTIFIER` is the name of the (un)defined string.
 *
 * E.g. `@string{ test = "Hello"}` and defined string `test` becomes `Hello`.
 */
fun BibtexDefinedString.evaluate(): String {
    val file = containingFile
    val stringName = text

    if (file == null) {
        return ""
    }

    // Look up all string entries.
    for (entry in file.childrenOfType(BibtexEntry::class)) {
        val token = entry.tokenName().lowercase(Locale.getDefault())
        if (token != "string") {
            continue
        }

        val tags = entry.tags()
        if (tags.isEmpty()) {
            continue
        }

        val tag = tags.first()
        val key = tag.keyName() ?: continue
        if (key != stringName) {
            continue
        }

        val content = tag.content ?: continue

        // Prevent perpetual recursion.
        if (key == content.text) {
            continue
        }

        return content.evaluate()
    }

    return "${'$'}$stringName${'$'}"
}

/**
 * Returns the contents of the braced string.
 *
 * E.g. `{{T}est $\alpha$ {H}ello}` becomes `{T}est $\alpha$ {H}ello`.
 */
fun BibtexBracedString.evaluate(): String {
    return text.substring(1 until text.length - 1)
}

/**
 * Returns the contents of the quoted string.
 *
 * E.g. `"Test Hello"` becomes `Test Hello`.
 */
fun BibtexQuotedString.evaluate(): String {
    return text.substring(1 until text.length - 1)
}

/**
 * Returns the contents of the quoted verbatim string.
 *
 * E.g. `"Test Hello"` becomes `Test Hello`.
 */
fun BibtexQuotedVerbatim.evaluate(): String = text.substring(1 until text.length - 1)

/**
 * Returns the contents of the braced verbatim string.
 *
 * E.g. `{Test Hello}` becomes `Test Hello`.
 */
fun BibtexBracedVerbatim.evaluate(): String = text.substring(1 until text.length - 1)