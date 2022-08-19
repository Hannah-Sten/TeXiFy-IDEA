package nl.hannahsten.texifyidea.lang.magic

import nl.hannahsten.texifyidea.lang.magic.TextBasedMagicCommentParser.Companion.COMMENT_PREFIX
import java.util.*

/**
 * Converts all provided strings to a single magic comments.
 *
 * Assumes that all provided comments start with the prefix [COMMENT_PREFIX].
 *
 * All keys are stored in lowercase.
 *
 * @author Hannah Schellekens
 */
open class TextBasedMagicCommentParser(private val comments: List<String>) : MagicCommentParser<String, String> {

    companion object {

        /**
         * The prefix that a magic comment has including the original comment %.
         *
         * This regex also matches whitespace after the tag.
         */
        @JvmStatic
        val COMMENT_PREFIX = Regex("""^(%\s*!\s*(TeX)?\s*)""", RegexOption.IGNORE_CASE)

        /**
         * Matches against a key assignment.
         */
        private val KEY_ASSIGNMENT = Regex("""^([a-zA-Z\s]+)\s*=""")
    }

    override fun parse(): MagicComment<String, String> = MutableMagicComment<String, String>().apply {
        // The key that is currently being processed, or null when there is no key yet.
        var key: String? = null

        // Collects the value of a key-value pair.
        var contentBuffer = StringBuilder()

        /** Adds the current key with converts to the magic comment. Also resets the key and content. */
        fun pushKeyValuePair() {
            if (key == null) return
            addValue(key!!.trim().asKey(), contentBuffer.toString().trim())
            contentBuffer = StringBuilder()
            key = null
        }

        comments.forEach { comment ->
            val line = comment.trimStart().replace(COMMENT_PREFIX, "").trimEnd()

            // Finish the key-value pair after an empty line is found.
            if (line.isEmpty()) {
                pushKeyValuePair()
                return@forEach
            }

            // Tries to find the beginning of a key assignment in the form of 'Key ='
            val keyMatcher = KEY_ASSIGNMENT.toPattern().matcher(line)

            // A key has been found, so a new key is created.
            when {
                keyMatcher.find() -> {
                    // Register previous key/value pair.
                    if (key != null) {
                        pushKeyValuePair()
                    }

                    key = keyMatcher.group(1).lowercase(Locale.getDefault())

                    val parts = line.split("=")
                    val contents = parts.subList(1, parts.size).joinToString("=")
                    contentBuffer.append(contents).append(' ')
                }
                // There is no key assignment.
                // Check if there is no previous key defined, because if there isn't, it will register an value-less key.
                key == null -> {
                    addValue(
                        key = line.split(" ").first().asKey(),
                        value = line.trim().split(" ").drop(1).joinToString(" ").let {
                            if (it.isEmpty()) null else it
                        }
                    )
                }
                // Fill up contents of the existing key.
                // Each newline is considered a space.
                else -> {
                    contentBuffer.append(line.trim()).append(' ')
                }
            }
        }

        pushKeyValuePair()
    }

    private fun String.asKey() = DefaultMagicKeys.values().find { it.key == this } ?: CustomMagicKey(this)
}

/**
 * Creates a new [TextBasedMagicCommentParser] from the list of magic comments.
 */
fun List<String>.textBasedMagicCommentParser() = TextBasedMagicCommentParser(this)