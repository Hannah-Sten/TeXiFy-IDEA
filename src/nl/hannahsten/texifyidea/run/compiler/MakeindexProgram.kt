package nl.hannahsten.texifyidea.run.compiler

/**
 * Makeindex programs (to create a .ind file).
 *
 * @author Thomas Schouten
 */
enum class MakeindexProgram(val executableName: String) {
    XINDY("texindy"),
    TRUEXINDY("xindy"),
    MAKEINDEX("makeindex")
}