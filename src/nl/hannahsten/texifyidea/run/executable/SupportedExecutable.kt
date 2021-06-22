package nl.hannahsten.texifyidea.run.executable

/**
 * Represents a program that can be executed.
 */
interface SupportedExecutable: Executable {
    val displayName: String
    val executableName: String
}