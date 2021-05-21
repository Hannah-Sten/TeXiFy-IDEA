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

    fun createStep(configuration: LatexRunConfiguration): CompileStep
}