package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import java.nio.file.Path

class LatexmkPathResolverTest : BasePlatformTestCase() {

    fun testResolveMainFileParentPlaceholder() {
        val mainFile = myFixture.addFileToProject("src/main.tex", "\\documentclass{article}").virtualFile
        val runConfig = createRunConfig(mainFile).apply {
            outputPathRaw = LatexmkPathResolver.MAIN_FILE_PARENT_PLACEHOLDER
        }

        val resolved = LatexmkPathResolver.resolveOutputDir(runConfig)
        assertEquals(Path.of(mainFile.parent.path), resolved)
    }

    fun testResolveProjectDirPlaceholder() {
        val mainFile = myFixture.addFileToProject("src/main.tex", "\\documentclass{article}").virtualFile
        val runConfig = createRunConfig(mainFile).apply {
            outputPathRaw = "${LatexmkPathResolver.PROJECT_DIR_PLACEHOLDER}/out"
        }

        val resolved = LatexmkPathResolver.resolveOutputDir(runConfig)
        assertTrue(resolved.toString().endsWith("/out"))
    }

    fun testResolveRelativePathsAgainstWorkingDirectory() {
        val mainFile = myFixture.addFileToProject("src/main.tex", "\\documentclass{article}").virtualFile
        val runConfig = createRunConfig(mainFile).apply {
            workingDirectory = Path.of(project.basePath!!, "build")
            outputPathRaw = "out"
            auxilPathRaw = "aux"
        }

        val resolved = LatexmkPathResolver.resolveOutAuxPair(runConfig) ?: error("Could not resolve directories")
        assertEquals(Path.of(project.basePath!!, "build", "out"), resolved.outputDir)
        assertEquals(Path.of(project.basePath!!, "build", "aux"), resolved.auxilDir)
        assertTrue(resolved.shouldPassAuxilDir)
    }

    fun testBlankOutputFallsBackToMainFileParentAndBlankAuxDisablesAuxdir() {
        val mainFile = myFixture.addFileToProject("src/main.tex", "\\documentclass{article}").virtualFile
        val runConfig = createRunConfig(mainFile).apply {
            outputPathRaw = ""
            auxilPathRaw = ""
        }

        val resolved = LatexmkPathResolver.resolveOutAuxPair(runConfig) ?: error("Could not resolve directories")
        assertEquals(Path.of(mainFile.parent.path), resolved.outputDir)
        assertNull(resolved.auxilDir)
        assertFalse(resolved.shouldPassAuxilDir)
    }

    private fun createRunConfig(mainFile: com.intellij.openapi.vfs.VirtualFile): LatexmkRunConfiguration =
        LatexmkRunConfiguration(
            project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk",
        ).apply {
            this.mainFile = mainFile
        }
}
