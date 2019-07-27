package nl.hannahsten.texifyidea.algorithm;

import java.util.List;

/**
 * A path algorithm calculates a path of some sort from a starting node to and end node.
 * <p>
 * Executing the algorithm
 *
 * @param <N>
 *         The type the nodes have.
 * @author Hannah Schellekens
 */
public interface PathAlgorithm<N> extends Algorithm {

    /**
     * Get the path that has been found by the algorithm.
     * <p>
     * {@link Algorithm#execute()} must have been executed prior.
     *
     * @return The path that was found by the algorithm.
     * @throws IllegalStateException
     *         When the algorithm hasn't been executed first using {@link Algorithm#execute()}.
     */
    List<N> getPath() throws IllegalStateException;

    /**
     * Get the length of the found path.
     * <p>
     * {@link Algorithm#execute()} must have been executed prior.
     *
     * @return The length of the found path.
     * @throws IllegalStateException
     *         When the algorithm hasn't been executed first using {@link Algorithm#execute()}.
     */
    default int size() throws IllegalArgumentException {
        return getPath().size();
    }
}
