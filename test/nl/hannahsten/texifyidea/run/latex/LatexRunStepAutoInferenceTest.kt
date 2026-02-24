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

        val augmented = LatexRunStepAutoInference.augmentStepTypes(
            runConfig,
            mainFile,
            listOf("latex-compile", "pdf-viewer")
        )

        assertEquals(listOf("latex-compile", "legacy-bibtex", "latex-compile", "pdf-viewer"), augmented)
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

        val augmented = LatexRunStepAutoInference.augmentStepTypes(
            runConfig,
            mainFile,
            listOf("latex-compile", "pdf-viewer")
        )

        assertEquals(listOf("latex-compile", "pythontex-command", "latex-compile", "pdf-viewer"), augmented)
    }

    fun testAugmentStepTypesDoesNotInsertLegacyBibliographyForLatexmkStep() {
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

        val augmented = LatexRunStepAutoInference.augmentStepTypes(
            runConfig,
            mainFile,
            listOf("latexmk-compile", "pdf-viewer")
        )

        assertEquals(listOf("latexmk-compile", "pdf-viewer"), augmented)
    }
}
