package nl.hannahsten.texifyidea.testutils

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsSettings
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsSettingsManager
import nl.hannahsten.texifyidea.settings.sdk.MiktexWindowsSdk
import nl.hannahsten.texifyidea.settings.sdk.TexliveSdk
import nl.hannahsten.texifyidea.util.selectedRunConfig
import org.apache.maven.artifact.versioning.DefaultArtifactVersion

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
    val runConfig = mockk<LatexRunConfiguration>()
    every { project.selectedRunConfig() } returns runConfig
    if (enabled) {
        // Unicode is always supported in lualatex.
        every { runConfig.primaryCompiler() } returns LatexCompiler.LUALATEX
    }
    else {
        // Unicode is not supported on pdflatex on texlive <= 2017.
        every { runConfig.primaryCompiler() } returns LatexCompiler.PDFLATEX
        mockkObject(TexliveSdk.Cache)
        every { TexliveSdk.Cache.version } returns 2017
        mockkConstructor(MiktexWindowsSdk::class)
        every { anyConstructed<MiktexWindowsSdk>().getVersion(null) } returns DefaultArtifactVersion("2.9.7300")
    }
}

fun String.toSystemNewLine() = replace(Regex("\n|\r\n"), System.lineSeparator())
