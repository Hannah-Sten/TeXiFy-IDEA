package nl.rubensten.texifyidea.algorithm;

/**
 * Why did I make an interface for this?
 * <p>
 * Cuz reasons.
 * <p>
 * Information about the algorithm can only be obtained after executing it with
 * {@link Algorithm#execute()}.
 *
 * @author Ruben Schellekens
 */
public interface Algorithm {

    /**
     * Executes the algorithm.
     * <p>
     * Information about the algorithm can only be obtained after executing it.
     */
    void execute();
}
