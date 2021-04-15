package nl.hannahsten.texifyidea.testutils

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.compiler.latex.LualatexCompiler
import nl.hannahsten.texifyidea.run.compiler.latex.PdflatexCompiler
import nl.hannahsten.texifyidea.settings.sdk.TexliveSdk
import nl.hannahsten.texifyidea.util.selectedRunConfig

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
 * Set the selected compiler in the selected run configuration and the Latex Distribution in a way to ensure either unicode
 * support, or no unicode support.
 */
fun setUnicodeSupport(project: Project, enabled: Boolean = true) {
    mockkStatic("nl.hannahsten.texifyidea.util.ProjectsKt")
    if (enabled) {
        mockkStatic(LatexRunConfiguration::class)
        // Unicode is always supported in lualatex.
        every { project.selectedRunConfig()?.compiler } returns LualatexCompiler
    }
    else {
        // Unicode is not supported on pdflatex on texlive <= 2017.
        every { project.selectedRunConfig()?.compiler } returns PdflatexCompiler
        mockkObject(TexliveSdk)
        every { TexliveSdk.version } returns 2017
    }
}
