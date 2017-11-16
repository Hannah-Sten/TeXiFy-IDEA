package nl.rubensten.texifyidea.documentation

import nl.rubensten.texifyidea.psi.BibtexKey

/**
 * @author Ruben Schellekens, Sten Wessel
 */
open class StringDeclarationLabel(val key: BibtexKey) : NavigationLabel<BibtexKey>(key) {

    override fun makeLabel(): String {
        val identifier = key.text
        return String.format("Go to declaration of string '%s' [line %d]", identifier, lineNumber())
    }
}