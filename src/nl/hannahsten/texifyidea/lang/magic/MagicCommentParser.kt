package nl.hannahsten.texifyidea.lang.magic

/**
 * @author Hannah Schellekens
 */
interface MagicCommentParser<Key, Value> {

    /**
     * Produces a proper magic comments.
     */
    fun parse(): MagicComment<Key, Value>
}