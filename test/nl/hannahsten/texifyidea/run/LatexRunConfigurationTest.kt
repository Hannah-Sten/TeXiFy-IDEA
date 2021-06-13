package nl.hannahsten.texifyidea.run

import com.intellij.ide.macro.ProjectFileDirMacro
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.compiler.latex.LatexmkCompiler
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationPathOption
import org.jdom.Element
import org.jdom.Namespace

class LatexRunConfigurationTest : BasePlatformTestCase() {

    fun testWriteRead() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        val element = Element("configuration", Namespace.getNamespace("", ""))
        runConfig.options.compiler = LatexmkCompiler
        runConfig.options.outputPath.setPath("${myFixture.project.basePath}/otherout", "\$${ProjectFileDirMacro().name}\$/otherout")
        runConfig.writeExternal(element)
        runConfig.readExternal(element)
        // Not sure if this actually tests anything todo
//        assertEquals(runConfig.options.compiler, LatexmkCompiler)
//        assertEquals(runConfig.outputPath.pathString, "\$${ProjectFileDirMacro().name}\$/otherout")
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
        runConfig.options.mainFile = LatexRunConfigurationPathOption("main.tex", "main.tex")
        runConfig.generateBibRunConfig()
//        assertTrue(runConfig.bibRunConfigs.isNotEmpty()) // todo
//        assertEquals(mainFile.virtualFile, (runConfig.bibRunConfigs.first().configuration as BibtexRunConfiguration).mainFile)
    }
}