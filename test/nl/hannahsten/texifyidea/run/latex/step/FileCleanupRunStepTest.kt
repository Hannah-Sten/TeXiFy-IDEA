package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import nl.hannahsten.texifyidea.run.latex.FileCleanupStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.LatexRunExecutionState
import java.nio.file.Files

class FileCleanupRunStepTest : BasePlatformTestCase() {

    fun testCleanupDeletesRegisteredFilesAndConsumesQueues() {
        val root = Files.createTempDirectory("texify-cleanup-step")
        val mainFilePath = root.resolve("main.tex")
        val toDelete = root.resolve("generated.tmp")
        val nestedParent = Files.createDirectories(root.resolve("tmp/parent"))
        val nestedChild = Files.createDirectories(nestedParent.resolve("child"))
        val nonEmpty = Files.createDirectories(root.resolve("tmp/non-empty"))
        Files.writeString(mainFilePath, "\\\\documentclass{article}")
        Files.writeString(toDelete, "temp")
        Files.writeString(nonEmpty.resolve("keep.txt"), "keep")

        val context = createContext(mainFilePath)
        context.executionState.addCleanupFile(toDelete)
        context.executionState.addCleanupDirectoriesIfEmpty(listOf(nestedParent, nestedChild, nonEmpty))

        val step = FileCleanupRunStep(FileCleanupStepOptions())
        step.beforeStart(context)
        val exitCode = step.runInline(context)
        step.afterFinish(context, exitCode)

        assertEquals(0, exitCode)
        assertFalse(Files.exists(toDelete))
        assertFalse(Files.exists(nestedChild))
        assertFalse(Files.exists(nestedParent))
        assertTrue(Files.exists(nonEmpty))
        assertTrue(context.executionState.filesToCleanUp.isEmpty())
        assertTrue(context.executionState.directoriesToDeleteIfEmpty.isEmpty())
    }

    fun testCleanupIgnoresMissingFiles() {
        val root = Files.createTempDirectory("texify-cleanup-step-missing")
        val mainFilePath = root.resolve("main.tex")
        Files.writeString(mainFilePath, "\\\\documentclass{article}")
        val missingFile = root.resolve("does-not-exist.tmp")

        val context = createContext(mainFilePath)
        context.executionState.addCleanupFile(missingFile)

        val step = FileCleanupRunStep(FileCleanupStepOptions())
        step.beforeStart(context)
        val exitCode = step.runInline(context)
        step.afterFinish(context, exitCode)

        assertEquals(0, exitCode)
        assertTrue(context.executionState.filesToCleanUp.isEmpty())
        assertTrue(context.executionState.directoriesToDeleteIfEmpty.isEmpty())
    }

    private fun createContext(mainFilePath: java.nio.file.Path): LatexRunStepContext {
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
