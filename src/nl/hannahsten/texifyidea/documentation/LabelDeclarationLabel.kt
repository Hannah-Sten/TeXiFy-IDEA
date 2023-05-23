package nl.hannahsten.texifyidea.documentation

import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.psi.requiredParameter

/**
 * @author Hannah Schellekens, Sten Wessel
 */
open class LabelDeclarationLabel(val command: LatexCommands) : NavigationLabel<LatexCommands>(command) {

    override fun makeLabel(): String {
        if ("\\label" == command.name) {
            val label = command.requiredParameter(0)
            return String.format("Go to declaration of label '%s' [%s:%d]", label, fileName(), lineNumber())
        }

        return "Unkown label"
    }
}