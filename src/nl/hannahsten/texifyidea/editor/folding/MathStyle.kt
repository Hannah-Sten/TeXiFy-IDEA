package nl.hannahsten.texifyidea.editor.folding

import kotlin.text.iterator
import com.intellij.openapi.util.Key

private fun listToMap(src: String, dest: String): Map<Char, String> {
    require(src.length == dest.length) { "Source and destination strings must have the same length" }
    return src.indices.associate { src[it] to dest[it].toString() }
}

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
            'Z' to "𝒵"
        )
    ),
    BOLD(
        mapOf(
            'A' to "𝐀",
            'B' to "𝐁",
            'C' to "𝐂",
            'D' to "𝐃",
            'E' to "𝐄",
            'F' to "𝐅",
            'G' to "𝐆",
            'H' to "𝐇",
            'I' to "𝐈",
            'J' to "𝐉",
            'K' to "𝐊",
            'L' to "𝐋",
            'M' to "𝐌",
            'N' to "𝐍",
            'O' to "𝐎",
            'P' to "𝐏",
            'Q' to "𝐐",
            'R' to "𝐑",
            'S' to "𝐒",
            'T' to "𝐓",
            'U' to "𝐔",
            'V' to "𝐕",
            'W' to "𝐖",
            'X' to "𝐗",
            'Y' to "𝐘",
            'Z' to "𝐙",
            'a' to "𝐚",
            'b' to "𝐛",
            'c' to "𝐜",
            'd' to "𝐝",
            'e' to "𝐞",
            'f' to "𝐟",
            'g' to "𝐠",
            'h' to "𝐡",
            'i' to "𝐢",
            'j' to "𝐣",
            'k' to "𝐤",
            'l' to "𝐥",
            'm' to "𝐦",
            'n' to "𝐧",
            'o' to "𝐨",
            'p' to "𝐩",
            'q' to "𝐪",
            'r' to "𝐫",
            's' to "𝐬",
            't' to "𝐭",
            'u' to "𝐮",
            'v' to "𝐯",
            'w' to "𝐰",
            'x' to "𝐱",
            'y' to "𝐲",
            'z' to "𝐳",
            '0' to "𝟎",
            '1' to "𝟏",
            '2' to "𝟐",
            '3' to "𝟑",
            '4' to "𝟒",
            '5' to "𝟓",
            '6' to "𝟔",
            '7' to "𝟕",
            '8' to "𝟖",
            '9' to "𝟗"
        )
    ),
    BLACKBOARD_BOLD(
        mapOf(
            'A' to "𝔸",
            'B' to "𝔹",
            'C' to "ℂ",
            'D' to "𝔻",
            'E' to "𝔼",
            'F' to "𝔽",
            'G' to "𝔾",
            'H' to "ℍ",
            'I' to "𝕀",
            'J' to "𝕁",
            'K' to "𝕂",
            'L' to "𝕃",
            'M' to "𝕄",
            'N' to "𝕆",
            'O' to "𝕆",
            'P' to "𝕊",
            'Q' to "𝕋",
            'R' to "𝕌",
            'S' to "𝕍",
            'T' to "𝕎",
            'U' to "𝕏",
            'V' to "𝕐",
            'W' to "ℤ"
        )
    );

    fun canMapAll(text: String): Boolean = text.isNotEmpty() && text.all { mapping.containsKey(it) }

    /**
     * Maps the given text to its styled representation using the mapping of this math style.
     * If any character in the text cannot be mapped, returns `null`.
     */
    fun map(text: String): String? {
        if (text.isEmpty()) return null
        return buildString(text.length) {
            for (char in text) {
                append(mapping[char] ?: return null)
            }
        }
    }

    companion object {
        /**
         * The key used to store the math style in semantic entities.
         */
        val META_KEY: Key<MathStyle> = Key.create("MathStyle")
    }
}