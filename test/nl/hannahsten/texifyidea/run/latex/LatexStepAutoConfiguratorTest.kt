package nl.hannahsten.texifyidea.run.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.step.LatexStepAutoConfigurator
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.updateFilesets

class LatexStepAutoConfiguratorTest : BasePlatformTestCase() {

    fun testCompleteStepsAddsBibliographyAndFollowUpCompile() {
        val mainPsi = myFixture.addFileToProject(
            "main-bib.tex",
            """
            \documentclass{article}
            \begin{document}
            \cite{knuth}
            \bibliography{references}
            \end{document}
            """.trimIndent()
        )
        val mainFile = mainPsi.virtualFile
        myFixture.updateFilesets()

        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.mainFilePath = mainFile.path
        runConfig.pdfViewer = PdfViewer.firstAvailableViewer

        val augmented = LatexStepAutoConfigurator.completeSteps(
            mainPsi,
            listOf(LatexCompileStepOptions(), PdfViewerStepOptions())
        )

        assertEquals(listOf("latex-compile", "bibtex", "latex-compile", "pdf-viewer"), augmented.map { it.type })
    }

    fun testCompleteStepsAddsPythontexTemplateStep() {
        val mainPsi = myFixture.addFileToProject(
            "main-pythontex.tex",
            """
            \documentclass{article}
            \usepackage{pythontex}
            \begin{document}
            \end{document}
            """.trimIndent()
        )
        val mainFile = mainPsi.virtualFile
        myFixture.updateFilesets()

        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.mainFilePath = mainFile.path

        val augmented = LatexStepAutoConfigurator.completeSteps(
            mainPsi,
            listOf(LatexCompileStepOptions(), PdfViewerStepOptions())
        )

        assertEquals(listOf("latex-compile", "pythontex", "latex-compile", "pdf-viewer"), augmented.map { it.type })
    }

    fun testCompleteStepsDoesNotInsertBibliographyForLatexmkStep() {
        val mainPsi = myFixture.addFileToProject(
            "main-latexmk-bib.tex",
            """
            \documentclass{article}
            \begin{document}
            \cite{knuth}
            \bibliography{references}
            \end{document}
            """.trimIndent()
        )
        val mainFile = mainPsi.virtualFile
        myFixture.updateFilesets()

        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.mainFilePath = mainFile.path

        val augmented = LatexStepAutoConfigurator.completeSteps(
            mainPsi,
            listOf(LatexmkCompileStepOptions(), PdfViewerStepOptions())
        )

        assertEquals(listOf("latexmk-compile", "pdf-viewer"), augmented.map { it.type })
    }

    fun testCompleteStepsAddsSecondClassicCompileWhenMainFileUnavailable() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )

        val completed = LatexStepAutoConfigurator.completeSteps(
            null,
            listOf(LatexCompileStepOptions(), PdfViewerStepOptions())
        )

        assertEquals(listOf("latex-compile", "latex-compile", "pdf-viewer"), completed.map { it.type })
    }

    fun testCompleteStepsUsesLatexmkDefaultsWhenBaseStepsEmpty() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )

        val completed = LatexStepAutoConfigurator.completeSteps(
            null,
            emptyList()
        )

        assertEquals(listOf("latexmk-compile", "pdf-viewer"), completed.map { it.type })
    }

    fun testCompleteStepsIsIdempotent() {
        val mainPsi = myFixture.addFileToProject(
            "main-idempotent.tex",
            """
            \documentclass{article}
            \begin{document}
            \cite{knuth}
            \bibliography{references}
            \end{document}
            """.trimIndent()
        )
        val mainFile = mainPsi.virtualFile
        myFixture.updateFilesets()

        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        myFixture.updateFilesets()
        runConfig.mainFilePath = mainFile.path

        val once = LatexStepAutoConfigurator.completeSteps(
            mainPsi,
            listOf(LatexCompileStepOptions(), PdfViewerStepOptions())
        )
        val twice = LatexStepAutoConfigurator.completeSteps(mainPsi, once)

        assertEquals(once.map { it.type }, twice.map { it.type })
    }

    fun testCompleteStepsDoesNotAddXindyAutomatically() {
        val mainPsi = myFixture.addFileToProject(
            "main-xindy.tex",
            """
            \documentclass{article}
            \usepackage[xindy]{imakeidx}
            \makeindex[xindy]
            \begin{document}
            \printindex
            \end{document}
            """.trimIndent()
        )
        myFixture.updateFilesets()

        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )

        val completed = LatexStepAutoConfigurator.completeSteps(
            mainPsi,
            listOf(LatexCompileStepOptions(), PdfViewerStepOptions())
        )

        assertEquals(listOf("latex-compile", "latex-compile", "pdf-viewer"), completed.map { it.type })
    }
}
