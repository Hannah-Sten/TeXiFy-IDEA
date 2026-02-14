package nl.hannahsten.texifyidea.editor.folding

import kotlin.text.iterator
import com.intellij.openapi.util.Key

/**
 * Maps plain characters to styled Unicode representations for math fonts.
 */
enum class MathStyle(
    private val mapping: Map<Char, String>
) {

    /**
     * The calligraphic math style, such as `\mathcal`.
     */
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

    /**
     * The bold math style, such as `\mathbf`.
     */
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

    /**
     * The blackboard bold math style, such as `\mathbb`.
     */
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
            'N' to "ℕ",
            'O' to "𝕆",
            'P' to "ℙ",
            'Q' to "ℚ",
            'R' to "ℝ",
            'S' to "𝕊",
            'T' to "𝕋",
            'U' to "𝕌",
            'V' to "𝕍",
            'W' to "𝕎",
            'X' to "𝕏",
            'Y' to "𝕐",
            'Z' to "ℤ",
            'a' to "𝕒",
            'b' to "𝕓",
            'c' to "𝕔",
            'd' to "𝕕",
            'e' to "𝕖",
            'f' to "𝕗",
            'g' to "𝕘",
            'h' to "𝕙",
            'i' to "𝕚",
            'j' to "𝕛",
            'k' to "𝕜",
            'l' to "𝕝",
            'm' to "𝕞",
            'n' to "𝕟",
            'o' to "𝕠",
            'p' to "𝕡",
            'q' to "𝕢",
            'r' to "𝕣",
            's' to "𝕤",
            't' to "𝕥",
            'u' to "𝕦",
            'v' to "𝕧",
            'w' to "𝕨",
            'x' to "𝕩",
            'y' to "𝕪",
            'z' to "𝕫",
            '0' to "𝟘",
            '1' to "𝟙",
            '2' to "𝟚",
            '3' to "𝟛",
            '4' to "𝟜",
            '5' to "𝟝",
            '6' to "𝟞",
            '7' to "𝟟",
            '8' to "𝟠",
            '9' to "𝟡"
        )
    ),

    /**
     * The Roman math style, which does not change the characters at all for better readability.
     * For example `L_{\mathrm{max}}` can be shown as `L_{max}`.
     * It is used for `\mathrm` and `\textup` math styles.
     */
    ROMAN(
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890".associateWith { it.toString() }
    ),

    /**
     * The sans-serif math style, such as `\mathsf`.
     *
     * Rendered as plain characters for better readability, similar to the Roman math style.
     */
    SANS_SERIF(
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890".associateWith { it.toString() }
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
         * If present at the metadata of a semantic command, it indicates that the command is a math style command such as `\mathrm` or `\mathcal`,
         * and the value indicates which math style it applies.
         */
        val META_KEY: Key<MathStyle> = Key.create("MathStyle")
    }
}