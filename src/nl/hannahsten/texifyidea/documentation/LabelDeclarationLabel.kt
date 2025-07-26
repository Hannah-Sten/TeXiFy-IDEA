package nl.hannahsten.texifyidea.documentation

import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * @author Hannah Schellekens, Sten Wessel
 */
open class LabelDeclarationLabel(val command: LatexCommands) : NavigationLabel<LatexCommands>(command) {

    override fun makeLabel(): String {
        if ("\\label" == command.name) {
            val label = command.requiredParameterText(0)
            return String.format("Go to declaration of label '%s' [%s:%d]", label, fileName(), lineNumber())
        }

        return "Unkown label"
    }
}