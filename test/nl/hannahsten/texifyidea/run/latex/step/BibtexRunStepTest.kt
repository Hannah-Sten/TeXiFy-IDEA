package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.configuration.EnvironmentVariablesData
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
import java.io.File
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
        var capturedEnvironment: Map<String, String>? = null
        mockkStatic("nl.hannahsten.texifyidea.run.common.CompilationProcessFactoryKt")
        every { createCompilationHandler(any(), any(), any(), any()) } answers {
            capturedCommand = secondArg()
            capturedWorkingDirectory = arg(2)
            capturedEnvironment = arg(3)
            expectedHandler
        }

        val process = BibtexRunStep(stepOptions).createProcess(context)

        assertEquals(expectedHandler, process)
        assertEquals(Path.of(context.session.outputDir.path), capturedWorkingDirectory)
        assertTrue(capturedCommand!!.contains("--input-directory=${Path.of(context.session.outputDir.path)}"))
        assertTrue(capturedCommand!!.contains("--output-directory=${Path.of(context.session.outputDir.path)}"))
        assertEquals(context.session.mainFile.parent.path, capturedEnvironment!!["BIBINPUTS"])
        assertEquals(context.session.mainFile.parent.path + File.pathSeparator, capturedEnvironment!!["BSTINPUTS"])
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
        every { createCompilationHandler(any(), any(), any(), any()) } answers {
            capturedWorkingDirectory = arg(2)
            expectedHandler
        }

        BibtexRunStep(stepOptions).createProcess(context)

        assertEquals(explicitWorkDir, capturedWorkingDirectory)
    }

    fun testBibtexAddsBibinputsWhenWorkingDirectoryDiffersFromMainFileDirOnTexlive() {
        val context = createContext()
        val stepOptions = BibtexStepOptions().apply {
            bibliographyCompiler = BibliographyCompiler.BIBTEX
        }
        val expectedHandler = mockk<KillableProcessHandler>(relaxed = true)
        var capturedEnvironment: Map<String, String>? = null
        mockkStatic("nl.hannahsten.texifyidea.run.common.CompilationProcessFactoryKt")
        every { createCompilationHandler(any(), any(), any(), any()) } answers {
            capturedEnvironment = arg(3)
            expectedHandler
        }

        BibtexRunStep(stepOptions).createProcess(context)

        assertEquals(context.session.mainFile.parent.path, capturedEnvironment!!["BIBINPUTS"])
        assertEquals(context.session.mainFile.parent.path + File.pathSeparator, capturedEnvironment!!["BSTINPUTS"])
    }

    fun testBibliographyStepDoesNotAddBibinputsWhenWorkingDirectoryMatchesMainFileDir() {
        val context = createContext(outputDirPath = null)
        val stepOptions = BibtexStepOptions().apply {
            bibliographyCompiler = BibliographyCompiler.BIBTEX
        }
        val expectedHandler = mockk<KillableProcessHandler>(relaxed = true)
        var capturedEnvironment: Map<String, String>? = null
        mockkStatic("nl.hannahsten.texifyidea.run.common.CompilationProcessFactoryKt")
        every { createCompilationHandler(any(), any(), any(), any()) } answers {
            capturedEnvironment = arg(3)
            expectedHandler
        }

        BibtexRunStep(stepOptions).createProcess(context)

        assertTrue(capturedEnvironment!!.isEmpty())
    }

    fun testBibliographyStepPrependsMainFileDirToExistingBibinputs() {
        val context = createContext(
            outputDirPath = Path.of("out"),
            environmentVariables = EnvironmentVariablesData.create(
                mapOf(
                    "BIBINPUTS" to "/custom/bib",
                    "BSTINPUTS" to "/custom/bst"
                ),
                true,
            )
        )
        val stepOptions = BibtexStepOptions().apply {
            bibliographyCompiler = BibliographyCompiler.BIBTEX
        }
        val expectedHandler = mockk<KillableProcessHandler>(relaxed = true)
        var capturedEnvironment: Map<String, String>? = null
        mockkStatic("nl.hannahsten.texifyidea.run.common.CompilationProcessFactoryKt")
        every { createCompilationHandler(any(), any(), any(), any()) } answers {
            capturedEnvironment = arg(3)
            expectedHandler
        }

        BibtexRunStep(stepOptions).createProcess(context)

        assertEquals(
            context.session.mainFile.parent.path + File.pathSeparator + "/custom/bib",
            capturedEnvironment!!["BIBINPUTS"]
        )
        assertEquals(
            context.session.mainFile.parent.path + File.pathSeparator + "/custom/bst",
            capturedEnvironment!!["BSTINPUTS"]
        )
    }

    fun testBibtexOnMiktexAddsIncludeDirectoryForMainFileDir() {
        val context = createContext(outputDirPath = Path.of("out"), distributionType = LatexDistributionType.MIKTEX)
        val stepOptions = BibtexStepOptions().apply {
            bibliographyCompiler = BibliographyCompiler.BIBTEX
        }
        val expectedHandler = mockk<KillableProcessHandler>(relaxed = true)
        var capturedCommand: List<String>? = null
        var capturedEnvironment: Map<String, String>? = null
        mockkStatic("nl.hannahsten.texifyidea.run.common.CompilationProcessFactoryKt")
        every { createCompilationHandler(any(), any(), any(), any()) } answers {
            capturedCommand = secondArg()
            capturedEnvironment = arg(3)
            expectedHandler
        }

        BibtexRunStep(stepOptions).createProcess(context)

        assertTrue(capturedCommand!!.contains("-include-directory=${context.session.mainFile.parent.path}"))
        assertTrue(capturedEnvironment!!.isEmpty())
    }

    private fun createContext(): LatexRunStepContext = createContext(outputDirPath = Path.of("out"))

    private fun createContext(
        outputDirPath: Path?,
        distributionType: LatexDistributionType = LatexDistributionType.TEXLIVE,
        environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT,
    ): LatexRunStepContext {
        val root = Files.createTempDirectory("texify-bibtex-step")
        val mainFilePath = root.resolve("main.tex")
        val resolvedOutputDirPath = outputDirPath?.let(root::resolve) ?: mainFilePath.parent
        val auxDirPath = root.resolve("aux")
        Files.createDirectories(resolvedOutputDirPath)
        Files.createDirectories(auxDirPath)
        Files.writeString(mainFilePath, "\\\\documentclass{article}")
        val mainFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(mainFilePath.toString())!!
        val outputDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(resolvedOutputDirPath.toString())!!
        val auxDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(auxDirPath.toString())!!

        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "test"
        )
        runConfig.environmentVariables = environmentVariables
        val environment = mockk<ExecutionEnvironment>(relaxed = true).also {
            every { it.project } returns project
        }
        val state = LatexRunSessionState(
            project = project,
            mainFile = mainFile,
            outputDir = outputDir,
            workingDirectory = Path.of(mainFile.parent.path),
            distributionType = distributionType,
            usesDefaultWorkingDirectory = true,
            latexSdk = null,
            auxDir = auxDir,
        )
        return LatexRunStepContext(runConfig, environment, state)
    }
}
