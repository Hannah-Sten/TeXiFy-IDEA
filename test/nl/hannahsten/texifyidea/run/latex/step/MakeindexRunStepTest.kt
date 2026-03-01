package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.mockk
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import nl.hannahsten.texifyidea.run.latex.MakeindexStepOptions
import com.intellij.execution.process.KillableProcessHandler
import java.nio.file.Files
import java.nio.file.Path

class MakeindexRunStepTest : BasePlatformTestCase() {

    fun testBuildCommandAppendsIdxForXindyWhenNoOutputOverride() {
        val context = createContext()
        val stepOptions = MakeindexStepOptions().apply {
            program = MakeindexProgram.XINDY
            targetBaseNameOverride = "custom-name"
            commandLineArguments = "-L english"
        }

        val command = MakeindexRunStep(stepOptions).buildCommand(context)

        assertEquals(listOf("texindy", "-L", "english", "custom-name.idx"), command)
    }

    fun testBuildCommandRespectsOutputOverride() {
        val context = createContext()
        val stepOptions = MakeindexStepOptions().apply {
            program = MakeindexProgram.MAKEINDEX
            targetBaseNameOverride = "custom-name"
            commandLineArguments = "-o result.ind"
        }

        val command = MakeindexRunStep(stepOptions).buildCommand(context)

        assertEquals(listOf("makeindex", "-o", "result.ind"), command)
    }

    fun testBib2glsDefaultsWorkingDirectoryToMainFileDirectory() {
        val context = createContext(separateAuxDir = true)
        val stepOptions = MakeindexStepOptions().apply {
            program = MakeindexProgram.BIB2GLS
        }

        val workingDirectory = MakeindexRunStep(stepOptions).resolveWorkingDirectory(context)

        assertEquals(Path.of(context.session.mainFile.parent.path), workingDirectory)
    }

    fun testLifecycleAndProcessCreationWorkForMakeindexStep() {
        val context = createContext()
        val stepOptions = MakeindexStepOptions().apply {
            program = MakeindexProgram.MAKEINDEX
            targetBaseNameOverride = "custom-name"
            workingDirectoryPath = context.session.mainFile.parent.path
        }
        val expectedHandler = mockk<KillableProcessHandler>(relaxed = true)
        mockkStatic("nl.hannahsten.texifyidea.run.common.CompilationProcessFactoryKt")
        every { createCompilationHandler(any(), any(), any()) } returns expectedHandler

        val step = MakeindexRunStep(stepOptions)
        step.beforeStart(context)
        val process = step.createProcess(context)
        step.afterFinish(context, 0)

        assertEquals(expectedHandler, process)
        assertEquals("makeindex", step.id)
        assertEquals(stepOptions.id, step.configId)
    }

    private fun createContext(separateAuxDir: Boolean = false): LatexRunStepContext {
        val root = Files.createTempDirectory("texify-makeindex-command")
        val mainFilePath = root.resolve("main.tex")
        Files.writeString(mainFilePath, "\\\\documentclass{article}")
        val mainFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(mainFilePath.toString())!!
        val auxDir = if (separateAuxDir) {
            val auxDirPath = root.resolve("aux")
            Files.createDirectories(auxDirPath)
            LocalFileSystem.getInstance().refreshAndFindFileByPath(auxDirPath.toString())!!
        }
        else {
            mainFile.parent
        }

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
            outputDir = auxDir,
            workingDirectory = Path.of(mainFile.parent.path),
            distributionType = LatexDistributionType.TEXLIVE,
            usesDefaultWorkingDirectory = true,
            latexSdk = null,
            auxDir = auxDir,
        )
        return LatexRunStepContext(runConfig, environment, state)
    }
}
