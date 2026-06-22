package nl.hannahsten.texifyidea.action

import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.pdfviewer.NoViewer
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.updateFilesets
import java.nio.file.Path

class ForwardSearchActionTest : BasePlatformTestCase() {

    fun testForwardSearchUsesRunConfigOfCurrentFileset() {
        myFixture.addFileToProject(
            "a/root-a.tex",
            """
            \documentclass{article}
            \begin{document}
            \input{chapter}
            \end{document}
            """.trimIndent()
        )
        val aChapter = myFixture.addFileToProject("a/chapter.tex", "A content")
        myFixture.addFileToProject(
            "b/root-b.tex",
            """
            \documentclass{article}
            \begin{document}
            \input{chapter}
            \end{document}
            """.trimIndent()
        )
        myFixture.addFileToProject("b/chapter.tex", "B content")
        myFixture.updateFilesets()

        addRunConfig("A", "a/root-a.tex", Path.of("{projectDir}", "out-a"))
        addRunConfig("B", "b/root-b.tex", Path.of("{projectDir}", "out-b"))

        // Using a mockk object would give an exception: java.lang.UnsupportedOperationException: class redefinition failed: attempted to change the schema
        val viewer = RecordingForwardSearchViewer()
        PdfViewer.additionalViewers = listOf(viewer)

        myFixture.openFileInEditor(aChapter.virtualFile)
        val textEditor = FileEditorManager.getInstance(project).selectedEditor as TextEditor

        ForwardSearchAction(viewer).actionPerformed(aChapter.virtualFile, project, textEditor)

        assertEquals(1, viewer.forwardSearchCalls.size)
        val call = viewer.forwardSearchCalls.single()
        assertTrue(call.outputPath?.endsWith("/a/root-a.pdf") == true)
        assertEquals(aChapter.virtualFile.path, call.sourceFilePath)
        assertEquals(project, call.project)
        assertTrue(call.focusAllowed)
    }

    private fun addRunConfig(name: String, mainFilePath: String, outputPath: Path): LatexRunConfiguration {
        val factory = LatexRunConfigurationProducer().configurationFactory
        val runConfig = LatexRunConfiguration(project, factory, name).apply {
            this.mainFilePath = mainFilePath
            this.outputPath = outputPath
            pdfViewer = NoViewer
        }
        val settings = RunManagerImpl.getInstanceImpl(project).createConfiguration(runConfig, factory)
        RunManagerImpl.getInstanceImpl(project).addConfiguration(settings)
        return settings.configuration as LatexRunConfiguration
    }

    private class RecordingForwardSearchViewer : PdfViewer {

        override val name: String = NoViewer.name
        override val displayName: String = NoViewer.displayName
        override val isForwardSearchSupported: Boolean = true

        val forwardSearchCalls = mutableListOf<ForwardSearchCall>()

        override fun isAvailable(): Boolean = true

        override fun forwardSearch(outputPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
            forwardSearchCalls += ForwardSearchCall(outputPath, sourceFilePath, line, project, focusAllowed)
        }
    }

    private data class ForwardSearchCall(
        val outputPath: String?,
        val sourceFilePath: String,
        val line: Int,
        val project: Project,
        val focusAllowed: Boolean,
    )
}
