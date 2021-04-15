package nl.hannahsten.texifyidea.run.legacy

/**
 * Other external tools which have to run between, before or after LaTeX runs, and that are not [MakeindexProgram]s or [BibtexCompiler]s.
 *
 * @author Thomas Schouten
 */
enum class ExternalTool(val executableName: String) {

    PYTHONTEX("pythontex")
}