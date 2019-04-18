package nl.rubensten.texifyidea.lang.magic

import nl.rubensten.texifyidea.lang.magic.TextBasedMagicCommentParser.Companion.COMMENT_PREFIX

/**
 * Converts all provided strings to a single magic comments.
 *
 * Assumes that all provided comments start with the prefix [COMMENT_PREFIX].
 *
 * @author Ruben Schellekens
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
        private val KEY_ASSIGNMENT = Regex("""^([a-zA-Z]+)\s*=""")
    }

    override fun parse(): MagicComment<String, String> = MutableMagicComment<String, String>().apply {
        // The key that is currently being processed, or null when there is no key yet.
        var key: String? = null

        // Collects the value of a key-value pair.
        var contentBuffer = StringBuilder()

        comments.forEach { comment ->
            val line = comment.replace(COMMENT_PREFIX, "")

            // Only consider proper lines.
            if (line.isEmpty()) return@forEach

            // Tries to find the beginning of a key assignment in the form of 'Key ='
            val keyMatcher = KEY_ASSIGNMENT.toPattern().matcher(line)

            // A key has been found, so a new key is created.
            if (keyMatcher.find()) {
                // Register previous key/value pair.
                if (key != null) {
                    addValue(key!!.asKey(), contentBuffer.toString().trimEnd())
                    contentBuffer = StringBuilder()
                }

                key = keyMatcher.group(1)

                val parts = line.split("=")
                val contents = parts.subList(1, parts.size).joinToString(" ") { it.trim() }
                contentBuffer.append(contents).append(' ')
            }
            // There is no key assignment.
            // Check if there is no previous key defined, because if there isn't, it will register an value-less key.
            else if (key == null) {
                addValue(line.split(" ").first().asKey(), null)
            }
            // Fill up contents of the existing key.
            // Each newline is considered a space.
            else {
                contentBuffer.append(line.trim()).append(' ')
            }
        }

        key?.let {
            addValue(it.asKey(), contentBuffer.toString().trimEnd())
        }
    }

    private fun String.asKey() = DefaultMagicKeys.values().find { it.key == this } ?: CustomMagicKey(this)
}