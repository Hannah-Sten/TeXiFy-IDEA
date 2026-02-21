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
            runConfig.setMainFile("main.tex")
        }

        runConfig.outputPath = Path.of("${LatexPathResolver.PROJECT_DIR_PLACEHOLDER}/out")
        val outPath = LatexPathResolver.resolveOutputDir(runConfig)
        assertNotNull(outPath)
        assertTrue(outPath!!.path.startsWith("/src"))
    }
}
