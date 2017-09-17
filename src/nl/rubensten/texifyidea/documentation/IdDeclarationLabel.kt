package nl.rubensten.texifyidea.documentation

import nl.rubensten.texifyidea.psi.BibtexId

/**
 * @author Ruben Schellekens, Sten Wessel
 */
open class IdDeclarationLabel(val id: BibtexId) : NavigationLabel<BibtexId>(id) {

    override fun makeLabel(): String {
        val identifier = id.name
        return String.format("Go to declaration of bibliography item '%s' [%s:%d]", identifier, fileName(), lineNumber())
    }
}