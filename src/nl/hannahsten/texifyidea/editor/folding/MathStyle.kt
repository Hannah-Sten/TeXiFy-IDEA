package nl.hannahsten.texifyidea.editor.folding

import kotlin.text.iterator

/**
 * Maps plain characters to styled Unicode representations for math fonts.
 */
enum class MathStyle(
    private val mapping: Map<Char, String>
) {
    CALLIGRAPHIC(
        mapOf(
            'A' to "𝒜",
            'B' to "ℬ",
            'C' to "𝒞",
            'D' to "𝒟",
            'E' to "ℰ",
            'F' to "ℱ",
            'G' to "𝒢",
            'H' to "ℋ",
            'I' to "ℐ",
            'J' to "𝒥",
            'K' to "𝒦",
            'L' to "ℒ",
            'M' to "ℳ",
            'N' to "𝒩",
            'O' to "𝒪",
            'P' to "𝒫",
            'Q' to "𝒬",
            'R' to "ℛ",
            'S' to "𝒮",
            'T' to "𝒯",
            'U' to "𝒰",
            'V' to "𝒱",
            'W' to "𝒲",
            'X' to "𝒳",
            'Y' to "𝒴",
            'Z' to "𝒵",
        )
    );

    fun canMapAll(text: String): Boolean = text.isNotEmpty() && text.all { mapping.containsKey(it) }

    fun map(text: String): String = buildString(text.length) {
        for (char in text) {
            append(mapping[char] ?: return text)
        }
    }
}