package nl.rubensten.texifyidea.lang

/**
 * @author Ruben Schellekens
 */
interface Dependend {

    /**
     * Get the myPackage that is required for the object to work.
     *
     * @return The myPackage object, or [Package.DEFAULT] when no myPackage is needed.
     */
    fun getDependency(): Package
}