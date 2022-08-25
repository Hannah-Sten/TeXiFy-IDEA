package nl.hannahsten.texifyidea.util

import nl.hannahsten.texifyidea.lang.commands.Argument
import nl.hannahsten.texifyidea.lang.commands.OptionalArgument

/**
 * Compute the optional power set of a set of arguments, preserving the ordering.
 *
 * Best explained with an example. Given a set ```[a]{b}{c}[d]```, it returns the sets
 *
 *  - ```{b}{c}```
 *  - ```[a]{b}{c}```
 *  - ```{b}{c}[d]```
 *  - ```[a]{b}{c}[d]```
 *
 */
fun Set<Argument>.optionalPowerSet(): Set<Set<Argument>> = setOf(this) +
    mapNotNull {
        if (it is OptionalArgument) filter { e -> e != it }.toSet().optionalPowerSet()
        else null
    }.flatten()
        .distinctBy { it.joinToString("") }
        .toSet()