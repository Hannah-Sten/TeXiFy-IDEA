package nl.hannahsten.texifyidea.run.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepAutoInference
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer

class LatexRunStepAutoInferenceTest : BasePlatformTestCase() {

    fun testAugmentStepTypesAddsBibliographyAndFollowUpCompile() {
        val mainFile = myFixture.addFileToProject(
            "main-bib.tex",
            """
            \documentclass{article}
            \begin{document}
            \cite{knuth}
            \bibliography{references}
            \end{document}
            """.trimIndent()
        ).virtualFile

        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.compiler = LatexCompiler.PDFLATEX
        runConfig.mainFilePath = mainFile.path
        runConfig.pdfViewer = PdfViewer.firstAvailableViewer

        val augmented = LatexRunStepAutoInference.augmentSteps(
            runConfig,
            mainFile,
            listOf(LatexCompileStepOptions(), PdfViewerStepOptions())
        )

        assertEquals(listOf("latex-compile", "bibtex", "latex-compile", "pdf-viewer"), augmented.map { it.type })
    }

    fun testAugmentStepTypesAddsPythontexTemplateStep() {
        val mainFile = myFixture.addFileToProject(
            "main-pythontex.tex",
            """
            \documentclass{article}
            \usepackage{pythontex}
            \begin{document}
            \end{document}
            """.trimIndent()
        ).virtualFile

        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.compiler = LatexCompiler.PDFLATEX
        runConfig.mainFilePath = mainFile.path

        val augmented = LatexRunStepAutoInference.augmentSteps(
            runConfig,
            mainFile,
            listOf(LatexCompileStepOptions(), PdfViewerStepOptions())
        )

        assertEquals(listOf("latex-compile", "pythontex", "latex-compile", "pdf-viewer"), augmented.map { it.type })
    }

    fun testAugmentStepTypesDoesNotInsertBibliographyForLatexmkStep() {
        val mainFile = myFixture.addFileToProject(
            "main-latexmk-bib.tex",
            """
            \documentclass{article}
            \begin{document}
            \cite{knuth}
            \bibliography{references}
            \end{document}
            """.trimIndent()
        ).virtualFile

        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.mainFilePath = mainFile.path

        val augmented = LatexRunStepAutoInference.augmentSteps(
            runConfig,
            mainFile,
            listOf(LatexmkCompileStepOptions(), PdfViewerStepOptions())
        )

        assertEquals(listOf("latexmk-compile", "pdf-viewer"), augmented.map { it.type })
    }
}
