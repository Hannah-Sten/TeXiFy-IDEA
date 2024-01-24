package nl.hannahsten.texifyidea.util

import com.intellij.openapi.util.TextRange
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import org.intellij.lang.annotations.Language
import java.io.File
import java.text.Normalizer
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Capitalises the first character of the string, if present.
 */
fun String.capitalizeFirst(): String = if (this.isEmpty()) this else this[0].uppercaseChar() + substring(1, length)

/**
 * Converts the string to camel case.
 */
fun String.camelCase(): String {
    @Language("RegExp")
    val parts = lowercase(Locale.getDefault()).split(Regex("[_\\s]+"))

    val sb = StringBuilder(parts[0])
    for (i in 1 until parts.size) {
        sb.append(parts[i].capitalizeFirst())
    }

    return sb.toString()
}

/**
 * Repeats the given string a given amount of times.
 *
 * @param count
 *         The amount of times to repeat the string.
 */
fun String.repeat(count: Int) = buildString(count * this.length) {
    for (i in 0 until count) {
        append(this@repeat)
    }
}

/**
 * Takes the substring, but with inverted index, i.e. the index of the first character is `length`, the last index is `0`.
 */
fun String.substringEnd(startIndex: Int): String = substring(0, length - startIndex)

/**
 * Takes the substring, but with inverted index, i.e. the index of the first character is `length`, the last index is `0`.
 */
fun String.substringEnd(startIndex: Int, endIndex: Int): String = substring(length - endIndex, length - startIndex)

/**
 * Takes the substring, but with inverted index, i.e. the index of the first character is `length`, the last index is `0`.
 */
fun String.substringEnd(range: IntRange): String = substringEnd(range.first, range.last + 1)

/**
 * Trims `startTrim` characters from the front, and `endTrim` characters from the end.
 */
fun String.trimRange(startTrim: Int, endTrim: Int): String {
    if (startTrim + endTrim > length) return ""
    return substring(startTrim).substringEnd(endTrim)
}

/**
 * Returns the leading whitespace of a string.
 */
fun String.getIndent(): String {
    val matcher = PatternMagic.leadingWhitespace.matcher(this)
    return if (matcher.find()) matcher.group(0) else ""
}

/**
 * Appends an extension to a path only if the given path does not end in that extension.
 *
 * @param extensionWithoutDot
 *         The extension to append optionally.
 * @return A path ending with the given extension without duplications (e.g. `.tex.tex` is impossible.)
 */
fun String.appendExtension(extensionWithoutDot: String): String {
    if (extensionWithoutDot == "") return this

    val dottedExtension = ".${extensionWithoutDot.lowercase(Locale.getDefault())}"
    val thisLower = lowercase(Locale.getDefault())

    return when {
        thisLower.endsWith(dottedExtension) -> this
        endsWith('.') -> this + extensionWithoutDot
        else -> this + dottedExtension
    }
}

/**
 * Removes unnecessary indents.
 *
 * Meaning that when all lines have at least `x` characters, `x` characters will be removed.
 *
 * @return All lines with shared indents removed.
 */
fun List<String>.removeIndents(): List<String> {
    if (isEmpty()) {
        return this
    }

    val list = ArrayList<String>(size)
    val (maxIndent, _) = asSequence()
        .filter { it.isNotBlank() }
        .map { Pair(it.getIndent().length, it) }
        .minByOrNull { it.first } ?: return this

    var noContentYet = true
    for (originalLine in this) {
        if (noContentYet && originalLine.isBlank()) {
            continue
        }

        if (originalLine.isBlank()) {
            list.add("")
        }
        else {
            noContentYet = false
            list.add(originalLine.substring(maxIndent))
        }
    }

    return list
}

/**
 * Removes unnecessary indents.
 *
 * Meaning that when all lines have at least `x` characters, `x` characters will be removed.
 *
 * @return All lines with shared indents removed.
 */
fun String.removeIndents() = PatternMagic.newline.split(this)
    .toList()
    .removeIndents()
    .joinToString("\n")

/**
 * Remove all appearances of all given strings.
 */
fun String.removeAll(vararg strings: String): String {
    var formatted = this
    strings.forEach { formatted = formatted.replace(it, "") }
    return formatted
}

/**
 * Remove [string] from [this].
 */
fun String.remove(string: String): String = this.replace(string, "")

/**
 * Replace everything after [string] - including [string] - from [this].
 */
fun String.replaceFrom(string: String, replacement: String) = this.replaceAfter(string, replacement).remove(string)

/**
 * Formats the string as a valid filename, removing not-allowed characters, in TeX-style with - as separator.
 */
fun String.formatAsFileName(): String = this.formatAsFilePath().removeAll("/", "\\")

/**
 * Formats the string as a valid filepath in our recommended LaTeX style, removing not-allowed characters, in TeX-style with - as separator. Any / or \ characters are not removed.
 */
fun String.formatAsFilePath(): String {
    val formatted = this.replace(" ", "-")
        .removeAll("<", ">", "\"", "|", "?", "*", ":") // Mostly just a problem on Windows
        .lowercase(Locale.getDefault())

    // If there are no valid characters left, use a default name.
    return formatted.ifEmpty { "myfile" }
}

/**
 * Formats the string as a valid LaTeX label name.
 */
fun String.formatAsLabel(): String {
    return this.let { Normalizer.normalize(it, Normalizer.Form.NFKD) }
        .replace(" ", "-")
        .removeAll("%", "~", "#", "\\", ",")
        .replace("[^\\x00-\\x7F]".toRegex(), "")
        .lowercase(Locale.getDefault())
}

/**
 * Split the given string on whitespace.
 */
fun String.splitWhitespace() = split(Regex("\\s+"))

/**
 * Removes HTML tags from the string.
 *
 * @return The string with HTML tags removed.
 *
 * @see [PatternMagic.htmlTag]
 */
fun String.removeHtmlTags() = this.replace(PatternMagic.htmlTag.toRegex(), "")

/**
 * Run a command in the terminal.
 * You can only use this if you are sure you don't have paths and other escaped things with spaces.
 *
 * @return The output of the command or null if an exception was thrown.
 */
fun String.runCommand(workingDirectory: File? = null) =
    runCommand(*(this.split("\\s".toRegex())).toTypedArray(), workingDirectory = workingDirectory)

fun String.runCommandWithExitCode(workingDirectory: File? = null) =
    runCommandWithExitCode(*(this.split("\\s".toRegex())).toTypedArray(), workingDirectory = workingDirectory)

/**
 * Index of first occurrence of any of the given chars. Return last index if chars do not appear in the string.
 */
fun String.firstIndexOfAny(vararg chars: Char): Int {
    var index = length - 1
    for (char in chars) {
        if (indexOf(char) != -1) index = min(index, indexOf(char))
    }
    return index
}

/** If this contains any of the given set. */
fun CharSequence.containsAny(set: Set<String>) = set.any { this.contains(it) }

/** If this starts with any of the given set. */
fun String.startsWithAny(vararg prefix: String) = prefix.any { this.startsWith(it) }

/** Shrink textrange with the given amount at both sides. */
fun TextRange.shrink(amount: Int) = TextRange(min(this.startOffset + amount, endOffset - 1), max(0, this.endOffset - amount))

/**
 * Appends a line separator.
 */
fun StringBuilder.newline() = append("\n")!!

/**
 * Encloses the string with the given prefix and suffix when the given predicate yields true.
 * Otherwise just returns this string.
 */
inline fun String.encloseWhen(prefix: String = "", suffix: String = "", predicate: () -> Boolean) = buildString {
    val predicateResult = predicate()
    if (predicateResult) {
        append(prefix)
    }
    append(this@encloseWhen)
    if (predicateResult) {
        append(suffix)
    }
}