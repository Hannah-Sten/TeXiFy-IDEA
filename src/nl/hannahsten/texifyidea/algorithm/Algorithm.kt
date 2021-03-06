package nl.hannahsten.texifyidea.algorithm

/**
 * Why did I make an interface for this?
 *
 *
 * Cuz reasons.
 *
 *
 * Information about the algorithm can only be obtained after executing it with
 * [Algorithm.execute].
 *
 * @author Hannah Schellekens
 */
interface Algorithm {

    /**
     * Executes the algorithm.
     *
     *
     * Information about the algorithm can only be obtained after executing it.
     */
    fun execute()
}