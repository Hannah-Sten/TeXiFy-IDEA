package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import nl.hannahsten.texifyidea.run.latex.BibtexStepOptions
import java.nio.file.Files
import java.nio.file.Path

class BibtexRunStepTest : BasePlatformTestCase() {

    fun testBiberUsesOutputDirectoryAsDefaultWorkingDirectoryOnTexlive() {
        val context = createContext()
        val stepOptions = BibtexStepOptions().apply {
            bibliographyCompiler = BibliographyCompiler.BIBER
        }
        val expectedHandler = mockk<KillableProcessHandler>(relaxed = true)
        var capturedCommand: List<String>? = null
        var capturedWorkingDirectory: Path? = null
        mockkStatic("nl.hannahsten.texifyidea.run.common.CompilationProcessFactoryKt")
        every { createCompilationHandler(any(), any(), any()) } answers {
            capturedCommand = secondArg()
            capturedWorkingDirectory = arg(2)
            expectedHandler
        }

        val process = BibtexRunStep(stepOptions).createProcess(context)

        assertEquals(expectedHandler, process)
        assertEquals(Path.of(context.session.outputDir.path), capturedWorkingDirectory)
        assertTrue(capturedCommand!!.contains("--input-directory=${Path.of(context.session.mainFile.parent.path)}"))
    }

    fun testBibtexHonorsConfiguredWorkingDirectory() {
        val context = createContext()
        val explicitWorkDir = Files.createTempDirectory("texify-bibtex-explicit")
        val stepOptions = BibtexStepOptions().apply {
            bibliographyCompiler = BibliographyCompiler.BIBER
            workingDirectoryPath = explicitWorkDir.toString()
        }
        val expectedHandler = mockk<KillableProcessHandler>(relaxed = true)
        var capturedWorkingDirectory: Path? = null
        mockkStatic("nl.hannahsten.texifyidea.run.common.CompilationProcessFactoryKt")
        every { createCompilationHandler(any(), any(), any()) } answers {
            capturedWorkingDirectory = arg(2)
            expectedHandler
        }

        BibtexRunStep(stepOptions).createProcess(context)

        assertEquals(explicitWorkDir, capturedWorkingDirectory)
    }

    private fun createContext(): LatexRunStepContext {
        val root = Files.createTempDirectory("texify-bibtex-step")
        val mainFilePath = root.resolve("main.tex")
        val outputDirPath = root.resolve("out")
        val auxDirPath = root.resolve("aux")
        Files.createDirectories(outputDirPath)
        Files.createDirectories(auxDirPath)
        Files.writeString(mainFilePath, "\\\\documentclass{article}")
        val mainFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(mainFilePath.toString())!!
        val outputDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(outputDirPath.toString())!!
        val auxDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(auxDirPath.toString())!!

        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "test"
        )
        val environment = mockk<ExecutionEnvironment>(relaxed = true).also {
            every { it.project } returns project
        }
        val state = LatexRunSessionState(
            project = project,
            mainFile = mainFile,
            outputDir = outputDir,
            workingDirectory = Path.of(mainFile.parent.path),
            distributionType = LatexDistributionType.TEXLIVE,
            usesDefaultWorkingDirectory = true,
            latexSdk = null,
            auxDir = auxDir,
        )
        return LatexRunStepContext(runConfig, environment, state)
    }
}
