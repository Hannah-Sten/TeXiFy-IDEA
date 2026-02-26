package nl.hannahsten.texifyidea.run.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.run.latex.step.LatexStepAutoConfigurator
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.updateFilesets

class LatexStepAutoConfiguratorTest : BasePlatformTestCase() {

    fun testCompleteStepsAddsBibliographyAndTwoFollowUpCompiles() {
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

        assertEquals(listOf("latex-compile", "bibtex", "latex-compile", "latex-compile", "pdf-viewer"), augmented.map { it.type })
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
        LatexRunConfiguration(
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
        LatexRunConfiguration(
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

    fun testCompleteStepsAddsFollowUpCompileAfterLastAuxBeforeViewer() {
        val completed = LatexStepAutoConfigurator.completeSteps(
            null,
            listOf(
                LatexCompileStepOptions(),
                PythontexStepOptions(),
                PdfViewerStepOptions(),
            )
        )

        assertEquals(listOf("latex-compile", "pythontex", "latex-compile", "pdf-viewer"), completed.map { it.type })
    }

    fun testCompleteStepsAddsMissingSecondCompileAfterBibliography() {
        val completed = LatexStepAutoConfigurator.completeSteps(
            null,
            listOf(
                LatexCompileStepOptions(),
                BibtexStepOptions(),
                LatexCompileStepOptions(),
                PdfViewerStepOptions(),
            )
        )

        assertEquals(listOf("latex-compile", "bibtex", "latex-compile", "latex-compile", "pdf-viewer"), completed.map { it.type })
    }

    fun testCompleteStepsWhenPsiInferenceFailsStillReturnsClosedPipeline() {
        val completed = LatexStepAutoConfigurator.completeSteps(
            null,
            listOf(LatexCompileStepOptions(), PdfViewerStepOptions())
        )

        assertEquals(listOf("latex-compile", "latex-compile", "pdf-viewer"), completed.map { it.type })
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

    fun testCompleteStepsAddsMakeindexStepForXindyPackages() {
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

        LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )

        val completed = LatexStepAutoConfigurator.completeSteps(
            mainPsi,
            listOf(LatexCompileStepOptions(), PdfViewerStepOptions())
        )

        assertEquals(listOf("latex-compile", "makeindex", "latex-compile", "pdf-viewer"), completed.map { it.type })
        val makeindex = completed.filterIsInstance<MakeindexStepOptions>().single()
        assertEquals(MakeindexProgram.XINDY, makeindex.program)
    }

    fun testCompleteStepsAddsMakeglossariesStepForGlossariesPackage() {
        val mainPsi = myFixture.addFileToProject(
            "main-glossaries.tex",
            """
            \documentclass{article}
            \usepackage{glossaries}
            \makeglossaries
            \begin{document}
            \end{document}
            """.trimIndent()
        )
        myFixture.updateFilesets()

        val completed = LatexStepAutoConfigurator.completeSteps(
            mainPsi,
            listOf(LatexCompileStepOptions(), PdfViewerStepOptions())
        )

        assertEquals(listOf("latex-compile", "makeglossaries", "latex-compile", "pdf-viewer"), completed.map { it.type })
        val makeglossaries = completed.filterIsInstance<MakeglossariesStepOptions>().single()
        assertTrue(makeglossaries.executable in setOf("makeglossaries", "makeglossaries-lite"))
    }

    fun testCompleteStepsAddsBib2glsForGlossariesExtraRecordOption() {
        val mainPsi = myFixture.addFileToProject(
            "main-bib2gls.tex",
            """
            \documentclass{article}
            \usepackage[record]{glossaries-extra}
            \begin{document}
            \end{document}
            """.trimIndent()
        )
        myFixture.updateFilesets()

        val completed = LatexStepAutoConfigurator.completeSteps(
            mainPsi,
            listOf(LatexCompileStepOptions(), PdfViewerStepOptions())
        )

        assertEquals(listOf("latex-compile", "makeindex", "latex-compile", "pdf-viewer"), completed.map { it.type })
        val makeindex = completed.filterIsInstance<MakeindexStepOptions>().single()
        assertEquals(MakeindexProgram.BIB2GLS, makeindex.program)
    }

    fun testCompleteStepsDoesNotAddIndexStepsForLatexmkPipeline() {
        val mainPsi = myFixture.addFileToProject(
            "main-latexmk-xindy.tex",
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

        val completed = LatexStepAutoConfigurator.completeSteps(
            mainPsi,
            listOf(LatexmkCompileStepOptions(), PdfViewerStepOptions())
        )

        assertEquals(listOf("latexmk-compile", "pdf-viewer"), completed.map { it.type })
        assertTrue(completed.none { it.type == LatexStepType.MAKEINDEX || it.type == LatexStepType.MAKEGLOSSARIES })
    }

    fun testCompleteStepsDoesNotDuplicateExplicitIndexSteps() {
        val mainPsi = myFixture.addFileToProject(
            "main-existing-index-step.tex",
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

        val explicitStep = MakeindexStepOptions().apply {
            program = MakeindexProgram.MAKEINDEX
        }
        val completed = LatexStepAutoConfigurator.completeSteps(
            mainPsi,
            listOf(LatexCompileStepOptions(), explicitStep, PdfViewerStepOptions())
        )

        assertEquals(listOf("latex-compile", "makeindex", "latex-compile", "pdf-viewer"), completed.map { it.type })
        val makeindexSteps = completed.filterIsInstance<MakeindexStepOptions>()
        assertEquals(1, makeindexSteps.size)
        assertEquals(MakeindexProgram.MAKEINDEX, makeindexSteps.single().program)
    }
}
