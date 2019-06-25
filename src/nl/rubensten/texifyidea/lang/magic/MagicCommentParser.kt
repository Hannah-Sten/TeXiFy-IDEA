package nl.rubensten.texifyidea.lang.magic

/**
 * @author Ruben Schellekens
 */
interface MagicCommentParser<Key, Value> {

    /**
     * Produces a proper magic comments.
     */
    fun parse(): MagicComment<Key, Value>
}