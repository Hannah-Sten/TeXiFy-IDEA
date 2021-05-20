package nl.hannahsten.texifyidea.run

import com.intellij.ide.macro.ProjectFileDirMacro
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.compiler.latex.LatexmkCompiler
import nl.hannahsten.texifyidea.run.legacy.bibtex.BibtexRunConfiguration
import org.jdom.Element
import org.jdom.Namespace

class LatexRunConfigurationTest : BasePlatformTestCase() {

    fun testWriteRead() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        val element = Element("configuration", Namespace.getNamespace("", ""))
        runConfig.getConfigOptions().compiler = LatexmkCompiler
        runConfig.outputPath.pathString = "\$${ProjectFileDirMacro().name}\$/otherout"
        runConfig.writeExternal(element)
        runConfig.readExternal(element)
        // Not sure if this actually tests anything
        assertEquals(runConfig.getConfigOptions().compiler, LatexmkCompiler)
        assertEquals(runConfig.outputPath.pathString, "\$${ProjectFileDirMacro().name}\$/otherout")
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