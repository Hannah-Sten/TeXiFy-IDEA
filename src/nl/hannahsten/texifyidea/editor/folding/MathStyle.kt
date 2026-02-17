package nl.hannahsten.texifyidea.editor.folding

import com.intellij.openapi.util.Key

private fun codePointString(codePoint: Int): String = String(Character.toChars(codePoint))

private fun mathAlphabetMapping(
    uppercaseStart: Int? = null,
    lowercaseStart: Int? = null,
    digitStart: Int? = null,
    plainDigits: Boolean = false,
    overrides: Map<Char, String> = emptyMap()
): Map<Char, String> {
    val mapping = LinkedHashMap<Char, String>(62)
    uppercaseStart?.let {
        for (i in 0 until 26) {
            mapping[('A'.code + i).toChar()] = codePointString(it + i)
        }
    }
    lowercaseStart?.let {
        for (i in 0 until 26) {
            mapping[('a'.code + i).toChar()] = codePointString(it + i)
        }
    }
    digitStart?.let {
        for (i in 0 until 10) {
            mapping[('0'.code + i).toChar()] = codePointString(it + i)
        }
    }
    if (plainDigits) {
        for (i in 0 until 10) {
            val digit = ('0'.code + i).toChar()
            mapping.putIfAbsent(digit, digit.toString())
        }
    }
    mapping.putAll(overrides)
    return mapping
}

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
        mathAlphabetMapping(
            uppercaseStart = 0x1D4D0,
            lowercaseStart = 0x1D4EA,
            plainDigits = true
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
            'e' to "ℯ",
            'f' to "𝒻",
            'g' to "ℊ",
            'h' to "𝒽",
            'i' to "𝒾",
            'j' to "𝒿",
            'k' to "𝓀",
            'l' to "𝓁",
            'm' to "𝓂",
            'n' to "𝓃",
            'o' to "ℴ",
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
            'z' to "𝓏",
            '0' to "0",
            '1' to "1",
            '2' to "2",
            '3' to "3",
            '4' to "4",
            '5' to "5",
            '6' to "6",
            '7' to "7",
            '8' to "8",
            '9' to "9"
        )
    ),

    /**
     * The bold math style, such as `\mathbf`.
     */
    BOLD(
        mathAlphabetMapping(
            uppercaseStart = 0x1D400,
            lowercaseStart = 0x1D41A,
            digitStart = 0x1D7CE
        )
    ),

    /**
     * The bold italic math style, such as `\symbfit` in `unicode-math`.
     */
    BOLD_ITALIC(
        mathAlphabetMapping(
            uppercaseStart = 0x1D468,
            lowercaseStart = 0x1D482,
            digitStart = 0x1D7CE
        )
    ),

    /**
     * The bold script math style, such as `\symbcal` and `\symbfscr` in `unicode-math`.
     */
    BOLD_SCRIPT(
        mathAlphabetMapping(
            uppercaseStart = 0x1D4D0,
            lowercaseStart = 0x1D4EA,
            digitStart = 0x1D7CE
        )
    ),

    /**
     * The bold fraktur math style, such as `\symbffrak` in `unicode-math`.
     */
    BOLD_FRAKTUR(
        mathAlphabetMapping(
            uppercaseStart = 0x1D56C,
            lowercaseStart = 0x1D586,
            digitStart = 0x1D7CE
        )
    ),

    /**
     * The bold upright sans-serif math style, such as `\symbfsfup` in `unicode-math`.
     */
    BOLD_SANS_SERIF_UPRIGHT(
        mathAlphabetMapping(
            uppercaseStart = 0x1D5D4,
            lowercaseStart = 0x1D5EE,
            digitStart = 0x1D7EC
        )
    ),

    /**
     * The bold italic sans-serif math style, such as `\symbfsfit` in `unicode-math`.
     */
    BOLD_SANS_SERIF_ITALIC(
        mathAlphabetMapping(
            uppercaseStart = 0x1D63C,
            lowercaseStart = 0x1D656,
            digitStart = 0x1D7EC
        )
    ),

    /**
     * The italic math style, such as `\mathit`.
     */
    ITALIC(
        mathAlphabetMapping(
            uppercaseStart = 0x1D434,
            lowercaseStart = 0x1D44E,
            plainDigits = true,
            overrides = mapOf(
                'h' to "ℎ"
            )
        )
    ),

    /**
     * The blackboard bold math style, such as `\mathbb`.
     *
     * A common usage is `\mathbb{R}` for the set of real numbers, which is rendered as `ℝ`.
     */
    BLACKBOARD_BOLD(
        mathAlphabetMapping(
            uppercaseStart = 0x1D538,
            lowercaseStart = 0x1D552,
            digitStart = 0x1D7D8,
            overrides = mapOf(
                'C' to "ℂ",
                'H' to "ℍ",
                'N' to "ℕ",
                'P' to "ℙ",
                'Q' to "ℚ",
                'R' to "ℝ",
                'Z' to "ℤ"
            )
        )
    ),

    /**
     * The fraktur math style, such as `\mathfrak`.
     */
    FRAKTUR(
        mathAlphabetMapping(
            uppercaseStart = 0x1D504,
            lowercaseStart = 0x1D51E,
            plainDigits = true,
            overrides = mapOf(
                'C' to "ℭ",
                'H' to "ℌ",
                'I' to "ℑ",
                'R' to "ℜ",
                'Z' to "ℨ"
            )
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
        mathAlphabetMapping(
            uppercaseStart = 0x1D670,
            lowercaseStart = 0x1D68A,
            digitStart = 0x1D7F6
        )
    );

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
