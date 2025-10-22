package nl.hannahsten.texifyidea.psi

import nl.hannahsten.texifyidea.lang.magic.*

/**
 * Get a [MagicComment] from a [LatexMagicComment] psi element. This magic comment always contains a single key and a single value.
 */
fun LatexMagicComment.getMagicComment(): MagicComment<String, String> =
    listOf(magicCommentToken.text).textBasedMagicCommentParser().parse()

/**
 * Get the [MagicKey] from a [LatexMagicComment] psi element.
 */
fun LatexMagicComment.key(): MagicKey<String> {
    val key = getMagicComment().keySet().firstOrNull() ?: ""
    return DefaultMagicKeys.entries.firstOrNull { it.key == key } ?: CustomMagicKey(key)
}

fun LatexMagicComment.containsKeyValuePair(key: String, value: String? = null): Boolean {
    return TextBasedMagicCommentParser.containsMagicCommentPair(magicCommentToken.text, key, value)
}

/**
 * Get the value from a [LatexMagicComment] psi element.
 */
fun LatexMagicComment.value(): String? = getMagicComment().value(key())