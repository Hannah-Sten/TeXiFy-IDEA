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
        listToMap(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
            "𝒜ℬ𝒞𝒟ℰℱ𝒢ℋℐ𝒥𝒦ℒℳ𝒩𝒪𝒫𝒬ℛ𝒮𝒯𝒰𝒱𝒲𝒳𝒴𝒵"
        )
    ),
    BOLD(
        listToMap(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",
            "𝐀𝐁𝐂𝐃𝐄𝐅𝐆𝐇𝐈𝐉𝐊𝐋𝐌𝐍𝐎𝐏𝐐𝐑𝐒𝐓𝐔𝐕𝐖𝐗𝐘𝐙" +
                "𝐚𝐛𝐜𝐝𝐞𝐟𝐠𝐡𝐢𝐣𝐤𝐥𝐦𝐧𝐨𝐩𝐪𝐫𝐬𝐭𝐮𝐯𝐰𝐱𝐲𝐳" +
                "𝟎𝟏𝟐𝟑𝟒𝟓𝟔𝟕𝟖𝟗"
        )
    ),
    BLACKBOARD_BOLD(
        listToMap(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",
            "𝔸𝔹ℂ𝔻𝔼𝔽𝔾ℍ𝕀𝕁𝕂𝕃𝕄ℕ𝕆ℙℚℝ𝕊𝕋𝕌𝕍𝕎𝕏𝕐ℤ" +
                "𝕒𝕓𝕔𝕕𝕖𝕗𝕘𝕙𝕚𝕛𝕜𝕝𝕞𝕟𝕠𝕡𝕢𝕣𝕤𝕥𝕦𝕧𝕨𝕩𝕪𝕫" +
                "𝟘𝟙𝟚𝟛𝟜𝟝𝟞𝟟𝟠𝟡"
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