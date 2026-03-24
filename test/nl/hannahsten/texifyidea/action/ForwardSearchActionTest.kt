package nl.hannahsten.texifyidea.action

import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import nl.hannahsten.texifyidea.updateFilesets
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.pdfviewer.NoViewer
import java.nio.file.Path

class ForwardSearchActionTest : BasePlatformTestCase() {

    override fun tearDown() {
        try {
            unmockkAll()
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

        addRunConfig("A", "a/root-a.tex", Path.of("{projectDir}", "out-a"))
        addRunConfig("B", "b/root-b.tex", Path.of("{projectDir}", "out-b"))

        mockkObject(NoViewer)
        every { NoViewer.isAvailable() } returns true
        every { NoViewer.isForwardSearchSupported } returns true
        every { NoViewer.forwardSearch(any(), any(), any(), any(), any()) } answers { }

        myFixture.openFileInEditor(aChapter.virtualFile)
        val textEditor = FileEditorManager.getInstance(project).selectedEditor as TextEditor

        ForwardSearchAction(NoViewer).actionPerformed(aChapter.virtualFile, project, textEditor)

        verify(exactly = 1) {
            NoViewer.forwardSearch(
                match { it.endsWith("/a/root-a.pdf") },
                aChapter.virtualFile.path,
                any(),
                project,
                true
            )
        }
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
}
