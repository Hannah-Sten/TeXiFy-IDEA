package nl.hannahsten.texifyidea.run.compiler.bibtex

import nl.hannahsten.texifyidea.run.legacy.bibtex.BibtexRunConfiguration

/**
 * @author Thomas Schouten
 */
object BiberCompiler : SupportedBibliographyCompiler("Biber", "biber") {

    override fun createCommand(runConfig: BibtexRunConfiguration): List<String>? = mutableListOf<String>().apply {
        add(runConfig.compilerPath ?: executableName)

        // Biber can find auxiliary files, but the flag is different from bibtex.
        // The following flag assumes the command is executed in the directory where the .bcf control file is.
        // The extra directory added is the directory from which the path to the .bib resource file is searched as specified in the .bcf file.
        add("--input-directory=${runConfig.mainFile?.parent?.path ?: ""}")

        runConfig.compilerArguments?.let { addAll(it.split("""\s+""".toRegex())) }

        add(runConfig.mainFile?.nameWithoutExtension ?: return null)
    }
}