package nl.hannahsten.texifyidea.documentation

import nl.hannahsten.texifyidea.psi.BibtexKey

/**
 * @author Hannah Schellekens, Sten Wessel
 */
open class StringDeclarationLabel(val key: BibtexKey) : NavigationLabel<BibtexKey>(key) {

    override fun makeLabel(): String {
        val identifier = key.text
        return String.format("Go to declaration of string '%s' [line %d]", identifier, lineNumber())
    }
}