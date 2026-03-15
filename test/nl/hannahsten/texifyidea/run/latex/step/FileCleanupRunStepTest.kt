package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import nl.hannahsten.texifyidea.run.latex.FileCleanupStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import java.nio.file.Files
import java.nio.file.Path

class FileCleanupRunStepTest : BasePlatformTestCase() {

    fun testCleanupDeletesRegisteredFilesAndConsumesQueues() {
        val root = Files.createTempDirectory("texify-cleanup-step")
        val mainFilePath = root.resolve("main.tex")
        val toDelete = root.resolve("generated.tmp")
        Files.writeString(mainFilePath, "\\\\documentclass{article}")
        Files.writeString(toDelete, "temp")

        val context = createContext(mainFilePath)
        context.session.addCleanupFile(toDelete)

        val step = FileCleanupRunStep(FileCleanupStepOptions())
        step.beforeStart(context)

        assertNull(step.createProcess(context))
        assertFalse(Files.exists(toDelete))
        assertTrue(context.session.filesToCleanUp.isEmpty())
    }

    fun testCleanupIgnoresMissingFiles() {
        val root = Files.createTempDirectory("texify-cleanup-step-missing")
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

    private fun createContext(mainFilePath: Path): LatexRunStepContext {
        val mainFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(mainFilePath.toString())!!
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
            outputDir = mainFile.parent,
            workingDirectory = Path.of(mainFile.parent.path),
            distributionType = LatexDistributionType.TEXLIVE,
            usesDefaultWorkingDirectory = true,
            latexSdk = null,
            auxDir = mainFile.parent,
        )
        return LatexRunStepContext(runConfig, environment, state)
    }
}
