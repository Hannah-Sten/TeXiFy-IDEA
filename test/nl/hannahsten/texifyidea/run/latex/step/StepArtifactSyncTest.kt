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

class StepArtifactSyncTest : BasePlatformTestCase() {

    fun testBib2glsCopiesDependenciesAndMovesOutputs() {
        val root = Files.createTempDirectory("texify-step-sync")
        val mainDir = Files.createDirectories(root.resolve("main"))
        val auxDir = Files.createDirectories(root.resolve("aux"))
        val workDir = Files.createDirectories(root.resolve("work"))
        val mainFilePath = mainDir.resolve("main.tex")
        Files.writeString(mainFilePath, "\\\\documentclass{article}\\n\\\\begin{document}x\\\\end{document}")

        Files.writeString(auxDir.resolve("main.aux"), "aux")
        Files.writeString(auxDir.resolve("main.glg"), "glg")
        Files.writeString(auxDir.resolve("main.log"), "log")

        val mainFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(mainFilePath.toString())!!
        val auxVirtual = LocalFileSystem.getInstance().refreshAndFindFileByPath(auxDir.toString())!!
        val outputVirtual = LocalFileSystem.getInstance().refreshAndFindFileByPath(auxDir.toString())!!

        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "test"
        )
        val environment = mockk<ExecutionEnvironment>(relaxed = true).also {
            every { it.project } returns project
        }
        val executionState = LatexRunExecutionState(
            resolvedMainFile = mainFile,
            resolvedAuxDir = auxVirtual,
            resolvedOutputDir = outputVirtual,
        )
        val context = LatexRunStepContext(runConfig, environment, executionState, mainFile)
        val step = MakeindexStepOptions().apply {
            program = MakeindexProgram.BIB2GLS
            targetBaseNameOverride = "main"
            workingDirectoryPath = workDir.toString()
        }
        val sync = StepArtifactSync(context, step)

        sync.beforeStep()

        assertTrue(Files.exists(workDir.resolve("main.aux")))
        assertTrue(Files.exists(workDir.resolve("main.glg")))
        assertTrue(Files.exists(workDir.resolve("main.log")))

        Files.writeString(workDir.resolve("main.ind"), "ind")
        Files.writeString(workDir.resolve("main.glo"), "glo")
        Files.writeString(workDir.resolve("main.glstex"), "glstex")
        Files.writeString(workDir.resolve("main.glg"), "glg")

        sync.afterStep(0)

        assertTrue(Files.exists(mainDir.resolve("main.ind")))
        assertTrue(Files.exists(mainDir.resolve("main.glo")))
        assertTrue(Files.exists(auxDir.resolve("main.glstex")))
        assertTrue(Files.exists(auxDir.resolve("main.glg")))
        assertFalse(Files.exists(workDir.resolve("main.glstex")))
    }
}
