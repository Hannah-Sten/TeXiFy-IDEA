package nl.hannahsten.texifyidea.run.compiler

internal object LatexCompilePrograms {

    const val LATEXMK_EXECUTABLE: String = "latexmk"

    val classicExecutableNames: Set<String>
        get() = LatexCompiler.entries.map { it.executableName }.toSet()

    val allExecutableNames: Set<String>
        get() = classicExecutableNames + LATEXMK_EXECUTABLE

    fun isLatexmkExecutable(executable: String?): Boolean =
        executable?.equals(LATEXMK_EXECUTABLE, ignoreCase = true) == true

    fun classicByExecutableName(executable: String?): LatexCompiler? = LatexCompiler.entries.firstOrNull {
        it.executableName.equals(executable, ignoreCase = true)
    }
}
