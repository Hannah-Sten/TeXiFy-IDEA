package nl.rubensten.texifyidea.lang

/**
 * @author Ruben Schellekens
 */
interface Dependend {

    /**
     * Get the package that is required for the object to work.
     *
     * @return The package object, or [Package.DEFAULT] when no package is needed.
     */
    val dependency: Package
}
