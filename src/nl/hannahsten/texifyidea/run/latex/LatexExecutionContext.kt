package nl.hannahsten.texifyidea.run.latex

import java.io.File

class LatexExecutionContext {
    val transientFilesToClean: MutableList<File> = mutableListOf()
    val emptyDirsToCleanup: MutableSet<File> = mutableSetOf()
    val flags: MutableMap<String, Boolean> = mutableMapOf()

    fun setFlag(name: String, value: Boolean) {
        flags[name] = value
    }

    fun getFlag(name: String): Boolean = flags[name] == true
}
