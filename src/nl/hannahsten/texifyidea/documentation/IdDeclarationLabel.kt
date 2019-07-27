package nl.hannahsten.texifyidea.documentation

import nl.hannahsten.texifyidea.psi.BibtexId

/**
 * @author Hannah Schellekens, Sten Wessel
 */
open class IdDeclarationLabel(val id: BibtexId) : NavigationLabel<BibtexId>(id) {

    override fun makeLabel(): String {
        val identifier = id.name
        return String.format("Go to declaration of bibliography item '%s' [%s:%d]", identifier, fileName(), lineNumber())
    }
}