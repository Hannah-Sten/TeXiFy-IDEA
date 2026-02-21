package nl.hannahsten.texifyidea.run

import com.intellij.psi.createSmartPointer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import nl.hannahsten.texifyidea.run.latex.LatexPathResolver
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import java.nio.file.Path

class LatexOutputPathTest : BasePlatformTestCase() {

    fun testOutputPathCreate() {
        val mainFile = myFixture.addFileToProject(
            "main.tex",
            """
            \documentclass{article}
            \begin{document}
                main
            \end{document}
            """.trimIndent()
        )
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.psiFile = mainFile.createSmartPointer()
        runBlocking {
            runConfig.mainFilePath = "main.tex"
        }

        runConfig.outputPath = Path.of("${LatexPathResolver.PROJECT_DIR_PLACEHOLDER}/out")
        val outPath = LatexPathResolver.resolveOutputDir(runConfig)
        assertNotNull(outPath)
        assertTrue(outPath!!.path.startsWith("/src"))
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
        runConfig.psiFile = mainFile.createSmartPointer()
        runBlocking {
            runConfig.mainFilePath = "sub/main.tex"
        }

        runConfig.outputPath = Path.of("non-existent-relative-dir")
        val resolved = LatexPathResolver.resolveOutputDir(runConfig)

        assertNotNull(resolved)
    }
}
