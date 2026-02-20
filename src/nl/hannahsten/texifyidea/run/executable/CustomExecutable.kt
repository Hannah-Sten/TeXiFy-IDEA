package nl.hannahsten.texifyidea.run.executable

/**
 * An [SupportedExecutable] but given by an absolute path instead of just the name.
 */
interface CustomExecutable : Executable {
    val executablePath: String
}