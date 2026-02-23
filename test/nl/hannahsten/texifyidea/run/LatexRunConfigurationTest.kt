package nl.hannahsten.texifyidea.run

import com.intellij.psi.createSmartPointer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexPathResolver
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.LatexmkModeService
import nl.hannahsten.texifyidea.run.latex.externaltool.ExternalToolRunConfigurationType
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import nl.hannahsten.texifyidea.run.latex.ui.LegacyLatexSettingsEditor
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.makeindex.MakeindexRunConfigurationType
import org.jdom.Element
import org.jdom.Namespace

class LatexRunConfigurationTest : BasePlatformTestCase() {

    fun testConfigurationEditorUsesFragmentedSettingsEditor() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        val editor = runConfig.configurationEditor
        assertTrue(editor is LatexSettingsEditor)
    }

    fun testWriteRead() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        val element = Element("configuration", Namespace.getNamespace("", ""))
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.outputPath = java.nio.file.Path.of("${LatexPathResolver.PROJECT_DIR_PLACEHOLDER}/otherout")
        runConfig.writeExternal(element)
        runConfig.readExternal(element)
        // Not sure if this actually tests anything
        assertEquals(runConfig.compiler, LatexCompiler.LATEXMK)
        assertEquals(runConfig.outputPath.toString(), "${LatexPathResolver.PROJECT_DIR_PLACEHOLDER}/otherout")
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
        runConfig.executionState.psiFile = mainFile.createSmartPointer()
        runBlocking {
            runConfig.mainFilePath = "main.tex"
        }
        runConfig.generateBibRunConfig()
        assertTrue(runConfig.bibRunConfigs.isNotEmpty())
        assertEquals(mainFile.virtualFile, (runConfig.bibRunConfigs.first().configuration as BibtexRunConfiguration).mainFile)
    }

    fun testBibRunConfigWithoutPsiPointerUsesResolvedMainFile() {
        val mainFile = myFixture.addFileToProject(
            "main.tex",
            """
            \documentclass{article}
            \begin{document}
                \cite{knuth1990}
                \bibliography{references}
            \end{document}
            """.trimIndent()
        )
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runBlocking {
            runConfig.mainFilePath = "main.tex"
        }
        runConfig.executionState.resolvedMainFile = mainFile.virtualFile
        runConfig.executionState.psiFile = null

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

        val editor = LegacyLatexSettingsEditor(project)
        editor.resetFrom(runConfig)

        val compilerField = LegacyLatexSettingsEditor::class.java.getDeclaredField("compiler")
        compilerField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val compiler = compilerField.get(editor) as com.intellij.openapi.ui.ComboBox<LatexCompiler>
        compiler.selectedItem = LatexCompiler.LATEXMK

        editor.applyTo(runConfig)

        assertTrue(runConfig.bibRunConfigs.isEmpty())
        assertTrue(runConfig.makeindexRunConfigs.isEmpty())
        assertTrue(runConfig.externalToolRunConfigs.isEmpty())
    }

    fun testLatexmkCompilerClearsCompileTwiceInEditorApply() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.compiler = LatexCompiler.PDFLATEX
        runConfig.compileTwice = true

        val editor = LegacyLatexSettingsEditor(project)
        editor.resetFrom(runConfig)

        val compilerField = LegacyLatexSettingsEditor::class.java.getDeclaredField("compiler")
        compilerField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val compiler = compilerField.get(editor) as com.intellij.openapi.ui.ComboBox<LatexCompiler>
        compiler.selectedItem = LatexCompiler.LATEXMK

        editor.applyTo(runConfig)

        assertFalse(runConfig.compileTwice)
    }

    fun testReadExternalIgnoresCompileTwiceForLatexmk() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        val element = Element("configuration", Namespace.getNamespace("", ""))
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.compileTwice = true
        runConfig.writeExternal(element)

        val restored = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Restored")
        restored.readExternal(element)

        assertEquals(LatexCompiler.LATEXMK, restored.compiler)
        assertFalse(restored.compileTwice)
    }

    fun testLatexmkCompilerHidesOutputFormatInEditor() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.compiler = LatexCompiler.LATEXMK

        val editor = LegacyLatexSettingsEditor(project)
        editor.resetFrom(runConfig)

        val outputFormatField = LegacyLatexSettingsEditor::class.java.getDeclaredField("outputFormatRow")
        outputFormatField.isAccessible = true
        val outputFormat = outputFormatField.get(editor) as javax.swing.JComponent
        assertFalse(outputFormat.isVisible)
    }

    fun testLatexmkCompilerShowsAuxPathRowInEditor() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.compiler = LatexCompiler.LATEXMK

        val editor = LegacyLatexSettingsEditor(project)
        editor.resetFrom(runConfig)

        val auxPathRowField = LegacyLatexSettingsEditor::class.java.getDeclaredField("auxilPathRow")
        auxPathRowField.isAccessible = true
        val auxPathRow = auxPathRowField.get(editor) as? javax.swing.JComponent
        assertNotNull(auxPathRow)
        assertTrue(auxPathRow!!.isVisible)
    }

    fun testAraraCompilerHidesAuxPathRowInEditor() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")

        val editor = LegacyLatexSettingsEditor(project)
        editor.resetFrom(runConfig)

        val compilerField = LegacyLatexSettingsEditor::class.java.getDeclaredField("compiler")
        compilerField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val compiler = compilerField.get(editor) as com.intellij.openapi.ui.ComboBox<LatexCompiler>
        compiler.selectedItem = LatexCompiler.ARARA

        val auxPathRowField = LegacyLatexSettingsEditor::class.java.getDeclaredField("auxilPathRow")
        auxPathRowField.isAccessible = true
        val auxPathRow = auxPathRowField.get(editor) as? javax.swing.JComponent
        assertNotNull(auxPathRow)
        assertFalse(auxPathRow!!.isVisible)
    }

    fun testLatexmkCompileModeEditorContainsAuto() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.compiler = LatexCompiler.LATEXMK

        val editor = LegacyLatexSettingsEditor(project)
        editor.resetFrom(runConfig)

        val compileModeField = LegacyLatexSettingsEditor::class.java.getDeclaredField("latexmkCompileMode")
        compileModeField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val compileMode = compileModeField.get(editor) as com.intellij.openapi.ui.ComboBox<LatexmkCompileMode>
        val items = (0 until compileMode.itemCount).map { compileMode.getItemAt(it) }
        assertTrue(items.contains(LatexmkCompileMode.AUTO))
    }

    fun testLatexmkCompileModeEditorApplyFallsBackToAuto() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.latexmkCompileMode = LatexmkCompileMode.PDFLATEX_PDF

        val editor = LegacyLatexSettingsEditor(project)
        editor.resetFrom(runConfig)

        val compileModeField = LegacyLatexSettingsEditor::class.java.getDeclaredField("latexmkCompileMode")
        compileModeField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val compileMode = compileModeField.get(editor) as com.intellij.openapi.ui.ComboBox<LatexmkCompileMode>
        compileMode.selectedItem = null

        editor.applyTo(runConfig)

        assertEquals(LatexmkCompileMode.AUTO, runConfig.latexmkCompileMode)
    }

    fun testLatexmkAutoCompileModeInEditorApplyUsesSelectedMainFilePackages() {
        val mainFile = myFixture.addFileToProject(
            "main-fontspec.tex",
            """
            \documentclass{article}
            \usepackage{fontspec}
            \begin{document}
            hi
            \end{document}
            """.trimIndent()
        )
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.latexmkCompileMode = LatexmkCompileMode.AUTO

        val editor = LegacyLatexSettingsEditor(project)
        editor.resetFrom(runConfig)

        val mainFileField = LegacyLatexSettingsEditor::class.java.getDeclaredField("mainFile")
        mainFileField.isAccessible = true
        val mainFileChooser = mainFileField.get(editor) as com.intellij.openapi.ui.TextFieldWithBrowseButton
        mainFileChooser.text = mainFile.virtualFile.name

        editor.applyTo(runConfig)

        assertEquals(mainFile.virtualFile.name, runConfig.mainFilePath)
        assertEquals(mainFile.virtualFile, nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport.resolveMainFile(runConfig))
        assertNull(runConfig.compilerArguments)
        runConfig.executionState.resolvedMainFile = nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport.resolveMainFile(runConfig)
        assertTrue(LatexmkModeService.buildArguments(runConfig).contains("-lualatex"))
    }

    fun testLatexmkAutoCompileModeFollowsMainFileChangeWhenPsiPointerIsStale() {
        val oldMain = myFixture.addFileToProject(
            "old-main.tex",
            """
            \documentclass{article}
            \begin{document}
            old
            \end{document}
            """.trimIndent()
        )
        val newMain = myFixture.addFileToProject(
            "new-main.tex",
            """
            \documentclass{ctexbeamer}
            \begin{document}
            new
            \end{document}
            """.trimIndent()
        )
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.latexmkCompileMode = LatexmkCompileMode.AUTO
        runConfig.mainFilePath = oldMain.virtualFile.name
        runConfig.executionState.psiFile = oldMain.createSmartPointer()

        val editor = LegacyLatexSettingsEditor(project)
        editor.resetFrom(runConfig)

        val mainFileField = LegacyLatexSettingsEditor::class.java.getDeclaredField("mainFile")
        mainFileField.isAccessible = true
        val mainFileChooser = mainFileField.get(editor) as com.intellij.openapi.ui.TextFieldWithBrowseButton
        mainFileChooser.text = newMain.virtualFile.name

        editor.applyTo(runConfig)

        assertEquals(newMain.virtualFile.name, runConfig.mainFilePath)
        assertEquals(newMain.virtualFile, nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport.resolveMainFile(runConfig))
        assertNull(runConfig.compilerArguments)
        runConfig.executionState.resolvedMainFile = nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport.resolveMainFile(runConfig)
        assertEquals(LatexmkCompileMode.XELATEX_PDF, LatexmkModeService.effectiveCompileMode(runConfig))
        assertTrue(LatexmkModeService.buildArguments(runConfig).contains("-xelatex"))
    }

    fun testResetEditorFromDoesNotMutateCompileTwiceOrLastRunFlag() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.compileTwice = true
        runConfig.executionState.isLastRunConfig = true

        val editor = LegacyLatexSettingsEditor(project)
        editor.resetFrom(runConfig)

        assertTrue(runConfig.compileTwice)
        assertTrue(runConfig.executionState.isLastRunConfig)
    }

    fun testWriteExternalStoresAuxConfigIdsInStructuredFormat() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        val runManager = com.intellij.execution.impl.RunManagerImpl.getInstanceImpl(project)
        val bib = runManager.createConfiguration("bib", LatexConfigurationFactory(BibtexRunConfigurationType()))
        runManager.addConfiguration(bib)
        runConfig.bibRunConfigs = setOf(bib)

        val element = Element("configuration", Namespace.getNamespace("", ""))
        runConfig.writeExternal(element)
        val parent = element.getChild("texify") ?: error("Missing texify node")
        val structured = parent.getChild("bib-run-configs") ?: error("Missing structured ids node")
        assertTrue(structured.getChildren("id").isNotEmpty())
    }

    fun testReadExternalSupportsLegacyAuxConfigSetFormat() {
        val runManager = com.intellij.execution.impl.RunManagerImpl.getInstanceImpl(project)
        val bib = runManager.createConfiguration("bib", LatexConfigurationFactory(BibtexRunConfigurationType()))
        runManager.addConfiguration(bib)

        val root = Element("configuration", Namespace.getNamespace("", ""))
        val parent = Element("texify")
        parent.addContent(Element("compiler").setText(LatexCompiler.PDFLATEX.name))
        parent.addContent(Element("main-file").setText(""))
        parent.addContent(Element("bib-run-config").setText("[${bib.uniqueID}]"))
        root.addContent(parent)

        val restored = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Restored")
        restored.readExternal(root)

        assertTrue(restored.bibRunConfigs.any { it.uniqueID == bib.uniqueID })
    }
}
