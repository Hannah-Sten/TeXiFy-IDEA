package nl.hannahsten.texifyidea.action

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.testutils.RecordingForwardSearchViewer
import nl.hannahsten.texifyidea.testutils.addLatexRunConfig
import nl.hannahsten.texifyidea.updateFilesets
import java.nio.file.Path

class ForwardSearchActionTest : BasePlatformTestCase() {

    override fun tearDown() {
        try {
            PdfViewer.additionalViewers = emptyList()
        }
        finally {
            super.tearDown()
        }
    }

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

        project.addLatexRunConfig("A", "a/root-a.tex", Path.of("{projectDir}", "out-a"))
        project.addLatexRunConfig("B", "b/root-b.tex", Path.of("{projectDir}", "out-b"))

        // Using a mockk object would give an exception: java.lang.UnsupportedOperationException: class redefinition failed: attempted to change the schema
        // This is probably because of intellij platform test instrumentation interfers with Mockk's one,
        // and it only happens for mocked methods that return Unit (see https://github.com/mockk/mockk/issues/1422)
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
}
