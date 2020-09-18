package nl.hannahsten.texifyidea.testutils

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import io.mockk.every
import io.mockk.mockkObject
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexDistribution
import nl.hannahsten.texifyidea.settings.TexifyProjectSettings

/**
 * Execute the given action as write command.
 * Can be used e.g. for running inspection QuickFixes
 *
 * @see WriteCommandAction
 * @see WriteCommandAction.Simple
 */
fun <T> writeCommand(project: Project, action: () -> T) {
    WriteCommandAction.writeCommandAction(project).compute<T, Exception> {
        action.invoke()
    }
}

/**
 * Set the TeXiFy Project Settings and the Latex Distribution in a way to ensure either unicode support, or no unicode support.
 */
fun setUnicodeSupport(project: Project, enabled: Boolean = true) {
    if (enabled) {
        // Unicode is always supported in lualatex.
        TexifyProjectSettings.getInstance(project).compilerCompatibility = LatexCompiler.LUALATEX
    }
    else {
        // Unicode is not supported on pdflatex on texlive <= 2017.
        TexifyProjectSettings.getInstance(project).compilerCompatibility = LatexCompiler.PDFLATEX
        mockkObject(LatexDistribution)
        every { LatexDistribution.texliveVersion } returns 2017
    }
}
