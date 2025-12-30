package nl.hannahsten.texifyidea.algorithm

/**
 * A path algorithm calculates a path of some sort from a starting node to and end node.
 *
 *
 * Executing the algorithm
 *
 * @param <N>
 * The type the nodes have.
 * @author Hannah Schellekens
</N> */
interface PathAlgorithm<N> : Algorithm {

    /**
     * Get the path that has been found by the algorithm.
     *
     *
     * [Algorithm.execute] must have been executed prior.
     *
     * @return The path that was found by the algorithm.
     * @throws IllegalStateException
     * When the algorithm hasn't been executed first using [Algorithm.execute].
     */
    @get:Throws(IllegalStateException::class)
    val path: List<N>

    /**
     * Get the length of the found path.
     *
     *
     * [Algorithm.execute] must have been executed prior.
     *
     * @return The length of the found path.
     * @throws IllegalStateException
     * When the algorithm hasn't been executed first using [Algorithm.execute].
     */
    @Throws(IllegalArgumentException::class)
    fun size(): Int = path.size
}