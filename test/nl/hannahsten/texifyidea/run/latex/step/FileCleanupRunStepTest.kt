package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.latex.FileCleanupStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import java.nio.file.Files
import java.nio.file.Path

class FileCleanupRunStepTest : BasePlatformTestCase() {

    fun testClassicCleanupDeletesTemporaryBuildArtifactsAndPreservesOutputs() {
        val root = Files.createTempDirectory("texify-cleanup-step")
        allowRootAccess(root)
        val mainDir = Files.createDirectories(root.resolve("src"))
        val outputDir = Files.createDirectories(root.resolve("out"))
        val auxDir = Files.createDirectories(root.resolve("aux"))
        val nestedOutputDir = Files.createDirectories(outputDir.resolve("nested"))
        val mainFilePath = mainDir.resolve("main.tex")
        val auxFile = mainDir.resolve("main.aux")
        val logFile = mainDir.resolve("main.log")
        val pdfFile = mainDir.resolve("main.pdf")
        val unrelatedAux = mainDir.resolve("other.aux")
        val queuedCopy = mainDir.resolve("queued.copy")
        val outputBbl = nestedOutputDir.resolve("main.bbl")
        val auxGlg = auxDir.resolve("main.glg")
        val outputXdv = outputDir.resolve("main.xdv")
        val outputPlainSynctex = outputDir.resolve("main.synctex")
        val outputSynctex = outputDir.resolve("main.synctex.gz")
        Files.writeString(mainFilePath, "\\\\documentclass{article}")
        Files.writeString(auxFile, "aux")
        Files.writeString(logFile, "log")
        Files.writeString(pdfFile, "pdf")
        Files.writeString(unrelatedAux, "other")
        Files.writeString(queuedCopy, "queued")
        Files.writeString(outputBbl, "bbl")
        Files.writeString(auxGlg, "glg")
        Files.writeString(outputXdv, "xdv")
        Files.writeString(outputPlainSynctex, "synctex")
        Files.writeString(outputSynctex, "synctex")

        val context = createContext(
            mainFilePath = mainFilePath,
            outputDirPath = outputDir,
            auxDirPath = auxDir,
            compileStep = LatexCompileStepOptions(),
        )
        context.session.addCleanupFile(queuedCopy)

        val step = FileCleanupRunStep(FileCleanupStepOptions())
        step.beforeStart(context)

        assertNull(step.createProcess(context))
        assertFalse(Files.exists(auxFile))
        assertFalse(Files.exists(logFile))
        assertFalse(Files.exists(queuedCopy))
        assertFalse(Files.exists(outputBbl))
        assertFalse(Files.exists(auxGlg))
        assertFalse(Files.exists(outputXdv))
        assertFalse(Files.exists(outputPlainSynctex))
        assertFalse(Files.exists(outputSynctex))
        assertTrue(Files.exists(pdfFile))
        assertTrue(Files.exists(unrelatedAux))
        assertTrue(context.session.filesToCleanUp.isEmpty())
    }

    fun testCleanupIgnoresMissingFiles() {
        val root = Files.createTempDirectory("texify-cleanup-step-missing")
        allowRootAccess(root)
        val mainFilePath = root.resolve("main.tex")
        Files.writeString(mainFilePath, "\\\\documentclass{article}")
        val missingFile = root.resolve("does-not-exist.tmp")

        val context = createContext(mainFilePath)
        context.session.addCleanupFile(missingFile)

        val step = FileCleanupRunStep(FileCleanupStepOptions())
        step.beforeStart(context)

        assertNull(step.createProcess(context))
        assertTrue(context.session.filesToCleanUp.isEmpty())
    }

    fun testClassicCleanupDoesNotRecursivelyScanMainDirectoryWhenUsedAsOutputDirectory() {
        val root = Files.createTempDirectory("texify-cleanup-main-dir")
        allowRootAccess(root)
        val mainDir = Files.createDirectories(root.resolve("src"))
        val nestedDir = Files.createDirectories(mainDir.resolve("nested"))
        val mainFilePath = mainDir.resolve("main.tex")
        val mainAux = mainDir.resolve("main.aux")
        val nestedAux = nestedDir.resolve("nested.aux")
        Files.writeString(mainFilePath, "\\\\documentclass{article}")
        Files.writeString(mainAux, "aux")
        Files.writeString(nestedAux, "nested")

        val context = createContext(
            mainFilePath = mainFilePath,
            outputDirPath = mainDir,
            auxDirPath = mainDir,
            compileStep = LatexCompileStepOptions(),
        )

        val step = FileCleanupRunStep(FileCleanupStepOptions())
        step.beforeStart(context)

        assertNull(step.createProcess(context))
        assertFalse(Files.exists(mainAux))
        assertTrue(Files.exists(nestedAux))
    }

    fun testLatexmkCleanupCreatesProcessWithCleanCommand() {
        val root = Files.createTempDirectory("texify-cleanup-latexmk")
        allowRootAccess(root)
        val mainDir = Files.createDirectories(root.resolve("src"))
        val outputDir = Files.createDirectories(root.resolve("out"))
        val auxDir = Files.createDirectories(root.resolve("aux"))
        val mainFilePath = mainDir.resolve("main.tex")
        Files.writeString(mainFilePath, "\\\\documentclass{article}")

        val latexmkStep = LatexmkCompileStepOptions().apply {
            compilerPath = "fake-latexmk"
        }
        val context = createContext(
            mainFilePath = mainFilePath,
            outputDirPath = outputDir,
            auxDirPath = auxDir,
            compileStep = latexmkStep,
        )

        val expectedHandler = mockk<KillableProcessHandler>(relaxed = true)
        var capturedCommand: List<String>? = null
        var capturedWorkingDirectory: Path? = null
        mockkStatic("nl.hannahsten.texifyidea.run.common.CompilationProcessFactoryKt")
        every { createCompilationHandler(any(), any(), any(), any()) } answers {
            capturedCommand = secondArg()
            capturedWorkingDirectory = arg(2)
            expectedHandler
        }

        val step = FileCleanupRunStep(FileCleanupStepOptions())
        step.beforeStart(context)
        val process = step.createProcess(context)

        assertEquals(expectedHandler, process)
        assertTrue(capturedCommand!!.contains("-c"))
        assertFalse(capturedCommand!!.contains("-C"))
        assertTrue(capturedCommand!!.any { it.startsWith("-outdir=") })
        assertTrue(capturedCommand!!.any { it.startsWith("-auxdir=") })
        assertEquals(context.session.workingDirectory, capturedWorkingDirectory)
    }

    fun testLatexmkCleanupDeletesQueuedCompatibilityFilesAfterSuccessfulRun() {
        val root = Files.createTempDirectory("texify-cleanup-latexmk-queue")
        allowRootAccess(root)
        val mainDir = Files.createDirectories(root.resolve("src"))
        val outputDir = Files.createDirectories(root.resolve("out"))
        val auxDir = Files.createDirectories(root.resolve("aux"))
        val mainFilePath = mainDir.resolve("main.tex")
        val queuedFile = mainDir.resolve("queued.copy")
        Files.writeString(mainFilePath, "\\\\documentclass{article}")
        Files.writeString(queuedFile, "queued")

        val context = createContext(
            mainFilePath = mainFilePath,
            outputDirPath = outputDir,
            auxDirPath = auxDir,
            compileStep = LatexmkCompileStepOptions().apply {
                compilerPath = "fake-latexmk"
            },
        )
        context.session.addCleanupFile(queuedFile)

        val step = FileCleanupRunStep(FileCleanupStepOptions())
        step.beforeStart(context)
        step.afterFinish(context, 0)

        assertFalse(Files.exists(queuedFile))
        assertTrue(context.session.filesToCleanUp.isEmpty())
    }

    private fun createContext(
        mainFilePath: Path,
        outputDirPath: Path? = null,
        auxDirPath: Path? = null,
        compileStep: nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions? = null,
    ): LatexRunStepContext {
        val mainFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(mainFilePath.toString())!!
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "test"
        )
        runConfig.mainFilePath = mainFilePath.toString()
        if (outputDirPath != null) {
            runConfig.outputPath = outputDirPath
        }
        if (auxDirPath != null) {
            runConfig.auxilPath = auxDirPath
        }
        runConfig.configOptions.steps = mutableListOf(
            compileStep ?: LatexCompileStepOptions()
        )
        val environment = mockk<ExecutionEnvironment>(relaxed = true).also {
            every { it.project } returns project
        }
        val outputDir = outputDirPath?.let { LocalFileSystem.getInstance().refreshAndFindFileByPath(it.toString()) }
            ?: mainFile.parent
        val auxDir = auxDirPath?.let { LocalFileSystem.getInstance().refreshAndFindFileByPath(it.toString()) }
            ?: mainFile.parent
        val state = LatexRunSessionState(
            project = project,
            mainFile = mainFile,
            outputDir = outputDir!!,
            workingDirectory = Path.of(mainFile.parent.path),
            distributionType = LatexDistributionType.TEXLIVE,
            usesDefaultWorkingDirectory = true,
            latexSdk = null,
            auxDir = auxDir,
        )
        return LatexRunStepContext(runConfig, environment, state)
    }

    private fun allowRootAccess(root: Path) {
        val roots = linkedSetOf(root.toAbsolutePath().normalize().toString())
        runCatching { root.toRealPath().toString() }.getOrNull()?.let(roots::add)
        VfsRootAccess.allowRootAccess(testRootDisposable, *roots.toTypedArray())
    }
}
