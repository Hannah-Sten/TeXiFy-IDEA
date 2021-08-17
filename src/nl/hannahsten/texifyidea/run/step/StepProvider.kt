package nl.hannahsten.texifyidea.run.step

import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import javax.swing.Icon

/**
 * When implementing a new compile step, be sure to register it in [nl.hannahsten.texifyidea.util.magic.CompilerMagic.compileStepProviders].
 */
interface StepProvider {

    val name: String

    val icon: Icon

    val id: String

    fun createStep(configuration: LatexRunConfiguration): Step

    /**
     * Check whether this step is required for a successful compilation.
     * If yes, return the created step, otherwise return null.
     * Implementations should ensure that this method does not duplicate any work (hence the 'if required' and 'create' step are asked for together).
     *
     * Whether a step is required may depend on other steps in the run config.
     */
    fun createIfRequired(runConfiguration: LatexRunConfiguration): List<Step>
}