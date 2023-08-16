package nl.hannahsten.texifyidea.testutils

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsSettings
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsSettingsManager
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
 * Update a convention setting and save the modified settings
 */
fun IdeaProjectTestFixture.updateConvention(action: (settings: TexifyConventionsSettings) -> Unit) {
    val settingsManager = TexifyConventionsSettingsManager.getInstance(this.project)
    val settings = settingsManager.getSettings()
    action(settings)
    settingsManager.saveSettings(settings)
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
        every { project.selectedRunConfig()?.compiler } returns LatexCompiler.LUALATEX
    }
    else {
        // Unicode is not supported on pdflatex on texlive <= 2017.
        every { project.selectedRunConfig()?.compiler } returns LatexCompiler.PDFLATEX
        mockkObject(TexliveSdk.Cache)
        every { TexliveSdk.Cache.version } returns 2017
    }
}

fun String.toSystemNewLine() = replace(Regex("\n|\r\n"), System.lineSeparator())