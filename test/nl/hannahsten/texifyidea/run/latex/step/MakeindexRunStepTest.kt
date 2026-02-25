package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.LatexRunExecutionState
import nl.hannahsten.texifyidea.run.latex.MakeindexStepOptions
import java.nio.file.Files

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

    fun testLifecycleAndProcessCreationWorkForMakeindexStep() {
        val context = createContext()
        val stepOptions = MakeindexStepOptions().apply {
            program = MakeindexProgram.MAKEINDEX
            targetBaseNameOverride = "custom-name"
            workingDirectoryPath = context.mainFile.parent.path
        }

        val step = MakeindexRunStep(stepOptions)
        step.beforeStart(context)
        val process = step.createProcess(context)
        step.afterFinish(context, 0)

        assertNotNull(process)
        assertEquals("makeindex", step.id)
        assertEquals(stepOptions.id, step.configId)
    }

    private fun createContext(): LatexRunStepContext {
        val root = Files.createTempDirectory("texify-makeindex-command")
        val mainFilePath = root.resolve("main.tex")
        Files.writeString(mainFilePath, "\\\\documentclass{article}")
        val mainFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(mainFilePath.toString())!!

        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "test"
        )
        val environment = mockk<ExecutionEnvironment>(relaxed = true).also {
            every { it.project } returns project
        }
        val state = LatexRunExecutionState(resolvedMainFile = mainFile)
        return LatexRunStepContext(runConfig, environment, state, mainFile)
    }
}
