package nl.rubensten.texifyidea.util

import org.intellij.lang.annotations.Language

/**
 * Capitalises the first character of the string.
 */
fun String.capitalizeFirst(): String = this[0].toUpperCase() + substring(1, length)

/**
 * Converts the string to camel case.
 */
fun String.camelCase(): String {
    @Language("RegExp")
    val parts = toLowerCase().split(Regex("[_\\s]+"))

    val sb = StringBuilder(parts[0])
    for (i in 1 until parts.size) {
        sb.append(parts[i].capitalizeFirst())
    }

    return sb.toString()
}