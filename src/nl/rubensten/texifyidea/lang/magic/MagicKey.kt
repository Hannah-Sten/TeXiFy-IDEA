package nl.rubensten.texifyidea.lang.magic

import nl.rubensten.texifyidea.lang.Described

/**
 * Represents a key from a [MagicComment]s.
 *
 * @author Ruben Schellekens
 */
interface MagicKey<Key> : Described {

    /**
     * The key used in [MagicComment].
     */
    val key: Key

    /**
     * Textual documentation about this key.
     *
     * The contents of this property contain some human-readable information about the key.
     */
    val documentation: String
        get() = ""

    /**
     * Denotes which scopes the magic key can target, i.e. in which contexts the keys can be used in a useful way.
     */
    val targets: Set<MagicCommentScope>
        get() = MagicCommentScope.ALL_SCOPES

    override val description: String
        get() = documentation
}