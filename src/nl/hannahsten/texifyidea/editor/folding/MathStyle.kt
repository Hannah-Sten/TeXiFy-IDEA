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
     * The script math style, such as `\mathscr`.
     */
    SCRIPT(
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
            'a' to "𝒶",
            'b' to "𝒷",
            'c' to "𝒸",
            'd' to "𝒹",
            'e' to "𝒺",
            'f' to "𝒻",
            'g' to "𝒼",
            'h' to "𝒽",
            'i' to "𝒾",
            'j' to "𝒿",
            'k' to "𝓀",
            'l' to "𝓁",
            'm' to "𝓂",
            'n' to "𝓃",
            'o' to "𝓄",
            'p' to "𝓅",
            'q' to "𝓆",
            'r' to "𝓇",
            's' to "𝓈",
            't' to "𝓉",
            'u' to "𝓊",
            'v' to "𝓋",
            'w' to "𝓌",
            'x' to "𝓍",
            'y' to "𝓎",
            'z' to "𝓏"
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
     * The italic math style, such as `\mathit`.
     */
    ITALIC(
        mapOf(
            'A' to "𝐴",
            'B' to "𝐵",
            'C' to "𝐶",
            'D' to "𝐷",
            'E' to "𝐸",
            'F' to "𝐹",
            'G' to "𝐺",
            'H' to "𝐻",
            'I' to "𝐼",
            'J' to "𝐽",
            'K' to "𝐾",
            'L' to "𝐿",
            'M' to "𝑀",
            'N' to "𝑁",
            'O' to "𝑂",
            'P' to "𝑃",
            'Q' to "𝑄",
            'R' to "𝑅",
            'S' to "𝑆",
            'T' to "𝑇",
            'U' to "𝑈",
            'V' to "𝑉",
            'W' to "𝑊",
            'X' to "𝑋",
            'Y' to "𝑌",
            'Z' to "𝑍",
            'a' to "𝑎",
            'b' to "𝑏",
            'c' to "𝑐",
            'd' to "𝑑",
            'e' to "𝑒",
            'f' to "𝑓",
            'g' to "𝑔",
            'h' to "ℎ",
            'i' to "𝑖",
            'j' to "𝑗",
            'k' to "𝑘",
            'l' to "𝑙",
            'm' to "𝑚",
            'n' to "𝑛",
            'o' to "𝑜",
            'p' to "𝑝",
            'q' to "𝑞",
            'r' to "𝑟",
            's' to "𝑠",
            't' to "𝑡",
            'u' to "𝑢",
            'v' to "𝑣",
            'w' to "𝑤",
            'x' to "𝑥",
            'y' to "𝑦",
            'z' to "𝑧"
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
     * The fraktur math style, such as `\mathfrak`.
     */
    FRAKTUR(
        mapOf(
            'A' to "𝔄",
            'B' to "𝔅",
            'C' to "ℭ",
            'D' to "𝔇",
            'E' to "𝔈",
            'F' to "𝔉",
            'G' to "𝔊",
            'H' to "ℌ",
            'I' to "ℑ",
            'J' to "𝔍",
            'K' to "𝔎",
            'L' to "𝔏",
            'M' to "𝔐",
            'N' to "𝔑",
            'O' to "𝔒",
            'P' to "𝔓",
            'Q' to "𝔔",
            'R' to "ℜ",
            'S' to "𝔖",
            'T' to "𝔗",
            'U' to "𝔘",
            'V' to "𝔙",
            'W' to "𝔚",
            'X' to "𝔛",
            'Y' to "𝔜",
            'Z' to "ℨ",
            'a' to "𝔞",
            'b' to "𝔟",
            'c' to "𝔠",
            'd' to "𝔡",
            'e' to "𝔢",
            'f' to "𝔣",
            'g' to "𝔤",
            'h' to "𝔥",
            'i' to "𝔦",
            'j' to "𝔧",
            'k' to "𝔨",
            'l' to "𝔩",
            'm' to "𝔪",
            'n' to "𝔫",
            'o' to "𝔬",
            'p' to "𝔭",
            'q' to "𝔮",
            'r' to "𝔯",
            's' to "𝔰",
            't' to "𝔱",
            'u' to "𝔲",
            'v' to "𝔳",
            'w' to "𝔴",
            'x' to "𝔵",
            'y' to "𝔶",
            'z' to "𝔷"
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
    ),

    /**
     * The monospace math style, such as `\mathtt`.
     */
    MONOSPACE(
        mapOf(
            'A' to "𝙰",
            'B' to "𝙱",
            'C' to "𝙲",
            'D' to "𝙳",
            'E' to "𝙴",
            'F' to "𝙵",
            'G' to "𝙶",
            'H' to "𝙷",
            'I' to "𝙸",
            'J' to "𝙹",
            'K' to "𝙺",
            'L' to "𝙻",
            'M' to "𝙼",
            'N' to "𝙽",
            'O' to "𝙾",
            'P' to "𝙿",
            'Q' to "𝚀",
            'R' to "𝚁",
            'S' to "𝚂",
            'T' to "𝚃",
            'U' to "𝚄",
            'V' to "𝚅",
            'W' to "𝚆",
            'X' to "𝚇",
            'Y' to "𝚈",
            'Z' to "𝚉",
            'a' to "𝚊",
            'b' to "𝚋",
            'c' to "𝚌",
            'd' to "𝚍",
            'e' to "𝚎",
            'f' to "𝚏",
            'g' to "𝚐",
            'h' to "𝚑",
            'i' to "𝚒",
            'j' to "𝚓",
            'k' to "𝚔",
            'l' to "𝚕",
            'm' to "𝚖",
            'n' to "𝚗",
            'o' to "𝚘",
            'p' to "𝚙",
            'q' to "𝚚",
            'r' to "𝚛",
            's' to "𝚜",
            't' to "𝚝",
            'u' to "𝚞",
            'v' to "𝚟",
            'w' to "𝚠",
            'x' to "𝚡",
            'y' to "𝚢",
            'z' to "𝚣",
            '0' to "𝟶",
            '1' to "𝟷",
            '2' to "𝟸",
            '3' to "𝟹",
            '4' to "𝟺",
            '5' to "𝟻",
            '6' to "𝟼",
            '7' to "𝟽",
            '8' to "𝟾",
            '9' to "𝟿"
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
         * If present at the metadata of a semantic command, it indicates that the command is a math style command such as `\mathrm` or `\mathcal`,
         * and the value indicates which math style it applies.
         */
        val META_KEY: Key<MathStyle> = Key.create("MathStyle")
    }
}