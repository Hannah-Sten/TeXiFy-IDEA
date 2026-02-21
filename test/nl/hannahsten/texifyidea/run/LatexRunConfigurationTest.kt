package nl.hannahsten.texifyidea.run

import com.intellij.psi.createSmartPointer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexOutputPath
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.externaltool.ExternalToolRunConfigurationType
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import nl.hannahsten.texifyidea.run.makeindex.MakeindexRunConfigurationType
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
        runConfig.psiFile = mainFile.createSmartPointer()
        runBlocking {
            runConfig.setMainFile("main.tex")
        }
        runConfig.generateBibRunConfig()
        assertTrue(runConfig.bibRunConfigs.isNotEmpty())
        assertEquals(mainFile.virtualFile, (runConfig.bibRunConfigs.first().configuration as BibtexRunConfiguration).mainFile)
    }

    fun testLatexmkCompilerClearsAuxiliaryRunConfigsInEditorApply() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        val runManager = com.intellij.execution.impl.RunManagerImpl.getInstanceImpl(project)

        val bib = runManager.createConfiguration("bib", LatexConfigurationFactory(BibtexRunConfigurationType()))
        val makeindex = runManager.createConfiguration("makeindex", LatexConfigurationFactory(MakeindexRunConfigurationType()))
        val external = runManager.createConfiguration("external", LatexConfigurationFactory(ExternalToolRunConfigurationType()))
        runManager.addConfiguration(bib)
        runManager.addConfiguration(makeindex)
        runManager.addConfiguration(external)

        runConfig.bibRunConfigs = setOf(bib)
        runConfig.makeindexRunConfigs = setOf(makeindex)
        runConfig.externalToolRunConfigs = setOf(external)

        val editor = LatexSettingsEditor(project)
        editor.resetFrom(runConfig)

        val compilerField = LatexSettingsEditor::class.java.getDeclaredField("compiler")
        compilerField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val compiler = compilerField.get(editor) as com.intellij.openapi.ui.LabeledComponent<com.intellij.openapi.ui.ComboBox<LatexCompiler>>
        compiler.component.selectedItem = LatexCompiler.LATEXMK

        editor.applyTo(runConfig)

        assertTrue(runConfig.bibRunConfigs.isEmpty())
        assertTrue(runConfig.makeindexRunConfigs.isEmpty())
        assertTrue(runConfig.externalToolRunConfigs.isEmpty())
    }
}
