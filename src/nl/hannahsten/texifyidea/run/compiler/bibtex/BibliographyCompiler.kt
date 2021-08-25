package nl.hannahsten.texifyidea.run.compiler.bibtex

import nl.hannahsten.texifyidea.run.compiler.Compiler
import nl.hannahsten.texifyidea.run.compiler.CustomCompiler
import nl.hannahsten.texifyidea.run.compiler.SupportedCompiler
import nl.hannahsten.texifyidea.run.step.BibliographyCompileStep
import nl.hannahsten.texifyidea.util.magic.CompilerMagic

sealed class BibliographyCompiler : Compiler<BibliographyCompileStep> {

    class Converter : com.intellij.util.xmlb.Converter<BibliographyCompiler>() {

        override fun toString(value: BibliographyCompiler) = when (value) {
            is SupportedBibliographyCompiler -> value.executableName
            is CustomBibliographyCompiler -> value.executablePath
        }

        override fun fromString(value: String): BibliographyCompiler {
            return SupportedBibliographyCompiler.byExecutableName(value) ?: CustomBibliographyCompiler(value)
        }
    }
}

class CustomBibliographyCompiler(override val executablePath: String) : BibliographyCompiler(),
                                                                        CustomCompiler<BibliographyCompileStep> {

    override fun getCommand(step: BibliographyCompileStep): List<String>? {
        return listOf(executablePath, step.state.mainFileName ?: return null)
    }
}

abstract class SupportedBibliographyCompiler(override val displayName: String, override val executableName: String) : BibliographyCompiler(), SupportedCompiler<BibliographyCompileStep> {

    abstract override fun getCommand(step: BibliographyCompileStep): List<String>?

    override fun toString() = this.displayName

    companion object {

        fun byExecutableName(exe: String) = CompilerMagic.bibliographyCompilerByExecutableName[exe]
    }
}