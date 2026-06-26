package nl.hannahsten.texifyidea.testutils

import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkConstructor
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.pdfviewer.NoViewer
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsSettings
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsSettingsManager
import nl.hannahsten.texifyidea.settings.sdk.MiktexWindowsSdk
import nl.hannahsten.texifyidea.settings.sdk.TexliveSdk
import nl.hannahsten.texifyidea.util.selectedRunConfig
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.nio.file.Path

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
 *
 * Tests that use this function must call `resetUnicodeSupportMocks()` during tearDown.
 */
fun setUnicodeSupport(project: Project, enabled: Boolean = true) {
    mockkStatic("nl.hannahsten.texifyidea.util.ProjectsKt")
    val runConfig = mockk<LatexRunConfiguration>()
    every { project.selectedRunConfig() } returns runConfig

    // getters that are implicitly called by the LatexForwardSearchIntention#isAvailable
    every { runConfig.mainFilePath } returns null
    every { runConfig.pdfViewer } returns null

    if (enabled) {
        // Unicode is always supported in lualatex.
        every { runConfig.primaryCompileStep() } returns LatexCompileStepOptions().apply { compiler = LatexCompiler.LUALATEX }
        every { runConfig.primaryCompiler() } returns LatexCompiler.LUALATEX
    }
    else {
        // Unicode is not supported on pdflatex on texlive <= 2017.
        every { runConfig.primaryCompileStep() } returns LatexCompileStepOptions().apply { compiler = LatexCompiler.PDFLATEX }
        every { runConfig.primaryCompiler() } returns LatexCompiler.PDFLATEX
        mockkObject(TexliveSdk.Cache)
        every { TexliveSdk.Cache.version } returns 2017
        mockkConstructor(MiktexWindowsSdk::class)
        every { anyConstructed<MiktexWindowsSdk>().getVersion(null) } returns DefaultArtifactVersion("2.9.7300")
    }
}

/**
 * Resets mocks that were set during `setUnicodeSupport()`.
 *
 * It should be called by all test classes that use `setUnicodeSupport()`.
 */
fun resetUnicodeSupportMocks() {
    runCatching { unmockkStatic("nl.hannahsten.texifyidea.util.ProjectsKt") }
    runCatching { unmockkObject(TexliveSdk.Cache) }
    runCatching { unmockkConstructor(MiktexWindowsSdk::class) }
}

fun String.toSystemNewLine() = replace(Regex("\n|\r\n"), System.lineSeparator())

/**
 * Adds a new LaTeX run configuration to the project.
 *
 * @param name The name of the run configuration.
 * @param mainFilePath The path to the main LaTeX file to be compiled.
 * @param outputPath The directory path where the outputs (e.g., PDF files) will be generated.
 * @return The created instance of [LatexRunConfiguration].
 */
fun Project.addLatexRunConfig(
    name: String,
    mainFilePath: String,
    outputPath: Path,
): LatexRunConfiguration {
    val factory = LatexRunConfigurationProducer().configurationFactory
    val runConfig = LatexRunConfiguration(this, factory, name).apply {
        this.mainFilePath = mainFilePath
        this.outputPath = outputPath
        pdfViewer = NoViewer
    }
    val settings = RunManagerImpl.getInstanceImpl(this).createConfiguration(runConfig, factory)
    RunManagerImpl.getInstanceImpl(this).addConfiguration(settings)
    return settings.configuration as LatexRunConfiguration
}
