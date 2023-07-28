package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexOutputPath
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import org.jdom.Element
import org.jdom.Namespace

class LatexRunConfigurationTest : BasePlatformTestCase() {

    fun testWriteRead() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        val element = Element("configuration", Namespace.getNamespace("", ""))
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.outputPath.pathString = LatexOutputPath.PROJECT_DIR_STRING + "otherout"
        runConfig.writeExternal(element)
        runConfig.readExternal(element)
        // Not sure if this actually tests anything
        assertEquals(runConfig.compiler, LatexCompiler.LATEXMK)
        assertEquals(runConfig.outputPath.pathString, LatexOutputPath.PROJECT_DIR_STRING + "otherout")
    }

    fun testBibRunConfig() {
        val mainFile = myFixture.addFileToProject(
            "main.tex",
            """
            \documentclass{article}
            \begin{document}
                When you are not looking at it, this sentences stops citing~\cite{knuth1990,goossens1993}.
                \bibliography{references}
                \bibliographystyle{plain}
            \end{document}
            """.trimIndent()
        )
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.psiFile = mainFile
        runConfig.setMainFile("main.tex")
        runConfig.generateBibRunConfig()
        assertTrue(runConfig.bibRunConfigs.isNotEmpty())
        assertEquals(mainFile.virtualFile, (runConfig.bibRunConfigs.first().configuration as BibtexRunConfiguration).mainFile)
    }
}