package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import nl.hannahsten.texifyidea.run.latex.LatexPathResolver
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import java.nio.file.Path

class LatexOutputPathTest : BasePlatformTestCase() {

    fun testOutputPathCreate() {
        myFixture.addFileToProject(
            "main.tex",
            """
            \documentclass{article}
            \begin{document}
                main
            \end{document}
            """.trimIndent()
        )
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runBlocking {
            runConfig.mainFilePath = "main.tex"
        }

        runConfig.outputPath = Path.of("${LatexPathResolver.PROJECT_DIR_PLACEHOLDER}/out")
        val outPath = LatexPathResolver.resolveOutputDir(runConfig)
        assertNotNull(outPath)
        assertTrue(outPath!!.path.startsWith("/src"))
    }

    fun testDefaultOutputAndAuxPathsResolveUnderProjectDir() {
        val mainFile = myFixture.addFileToProject(
            "sub/main.tex",
            """
            \documentclass{article}
            \begin{document}
                main
            \end{document}
            """.trimIndent()
        )
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runBlocking {
            runConfig.mainFilePath = "sub/main.tex"
        }

        val projectRoot = requireNotNull(LatexPathResolver.getMainFileContentRoot(mainFile.virtualFile, project)).path
        val outputDir = LatexPathResolver.resolve(runConfig.outputPath, mainFile.virtualFile, project)
        val auxDir = LatexPathResolver.resolve(runConfig.auxilPath, mainFile.virtualFile, project)

        assertEquals(Path.of(projectRoot, "out"), outputDir)
        assertEquals(Path.of(projectRoot, "out"), auxDir)
    }

    fun testRelativeOutputPathResolvesViaContentRoot() {
        val mainFile = myFixture.addFileToProject(
            "sub/main.tex",
            """
            \documentclass{article}
            \begin{document}
                main
            \end{document}
            """.trimIndent()
        )
        val marker = myFixture.addFileToProject("build/out/.keep", "")
        val expectedPath = marker.virtualFile.parent.path

        val resolved = LatexPathResolver.resolve(
            Path.of("build/out"),
            mainFile.virtualFile,
            project
        )

        assertEquals(expectedPath, resolved?.toString())
    }

    fun testUnresolvedRelativeOutputPathFallsBackWithoutCrash() {
        myFixture.addFileToProject(
            "sub/main.tex",
            """
            \documentclass{article}
            \begin{document}
                main
            \end{document}
            """.trimIndent()
        )

        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runBlocking {
            runConfig.mainFilePath = "sub/main.tex"
        }

        runConfig.outputPath = Path.of("non-existent-relative-dir")
        val resolved = LatexPathResolver.resolveOutputDir(runConfig)

        assertNotNull(resolved)
    }
}
