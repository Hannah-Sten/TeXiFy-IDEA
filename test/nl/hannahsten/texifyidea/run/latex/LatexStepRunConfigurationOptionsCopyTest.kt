package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ui.FragmentedSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode

class LatexStepRunConfigurationOptionsCopyTest : BasePlatformTestCase() {

    fun testDeepCopyPreservesFieldsForAllStepTypes() {
        val originals = listOf(
            LatexCompileStepOptions().apply {
                id = "compile-1"
                enabled = false
                compiler = LatexCompiler.LUALATEX
                compilerPath = "/usr/bin/lualatex"
                compilerArguments = "-shell-escape"
                outputFormat = LatexCompiler.Format.DVI
                beforeRunCommand = "echo pre"
                selectedOptions.add(FragmentedSettings.Option("compile.args", true))
            },
            LatexmkCompileStepOptions().apply {
                id = "latexmk-1"
                compilerPath = "/usr/bin/latexmk"
                compilerArguments = "-gg"
                latexmkCompileMode = LatexmkCompileMode.CUSTOM
                latexmkCustomEngineCommand = "xelatex"
                latexmkCitationTool = LatexmkCitationTool.BIBER
                latexmkExtraArguments = "-interaction=nonstopmode"
                beforeRunCommand = "echo mk"
                selectedOptions.add(FragmentedSettings.Option("latexmk.mode", true))
            },
            PdfViewerStepOptions().apply {
                id = "viewer-1"
                pdfViewerName = "Custom Viewer"
                requireFocus = false
                customViewerCommand = "open {pdf}"
                selectedOptions.add(FragmentedSettings.Option("viewer.command", true))
            },
            BibtexStepOptions().apply {
                id = "bibtex-1"
                bibliographyCompiler = BibliographyCompiler.BIBER
                compilerPath = "/usr/bin/biber"
                compilerArguments = "--quiet"
                workingDirectoryPath = "/tmp"
                beforeRunCommand = "echo bib"
                selectedOptions.add(FragmentedSettings.Option("bibtex.args", true))
            },
            MakeindexStepOptions().apply {
                id = "makeindex-1"
                program = MakeindexProgram.XINDY
                commandLineArguments = "-L english"
                workingDirectoryPath = "/tmp"
                targetBaseNameOverride = "custom"
                beforeRunCommand = "echo idx"
                selectedOptions.add(FragmentedSettings.Option("makeindex.args", true))
            },
            ExternalToolStepOptions().apply {
                id = "external-1"
                executable = "/usr/bin/custom-tool"
                arguments = "--flag"
                workingDirectoryPath = "/work"
                beforeRunCommand = "echo ext"
                selectedOptions.add(FragmentedSettings.Option("external.args", true))
            },
            PythontexStepOptions().apply {
                id = "pythontex-1"
                executable = "pythontex3"
                arguments = "--interpreter python3"
                workingDirectoryPath = "/work"
                beforeRunCommand = "echo py"
                selectedOptions.add(FragmentedSettings.Option("pythontex.args", true))
            },
            MakeglossariesStepOptions().apply {
                id = "glossaries-1"
                executable = "makeglossaries-lite"
                arguments = "--verbose"
                workingDirectoryPath = "/work"
                beforeRunCommand = "echo gloss"
                selectedOptions.add(FragmentedSettings.Option("makeglossaries.args", true))
            },
            XindyStepOptions().apply {
                id = "xindy-1"
                executable = "texindy"
                arguments = "-L english"
                workingDirectoryPath = "/work"
                beforeRunCommand = "echo xindy"
                selectedOptions.add(FragmentedSettings.Option("xindy.args", true))
            }
        )

        originals.forEach { original ->
            val copied = original.deepCopy()
            assertNotSame(original, copied)
            assertEquals(original.type, copied.type)
            assertEquals(original.id, copied.id)
            assertEquals(original.enabled, copied.enabled)
            assertEquals(original.selectedOptions.single().name, copied.selectedOptions.single().name)
            assertNotSame(original.selectedOptions, copied.selectedOptions)
            assertNotSame(original.selectedOptions.single(), copied.selectedOptions.single())
            assertStepFieldsEqual(original, copied)
        }
    }

    fun testDeepCopyDoesNotShareSelectedOptions() {
        val original = LatexCompileStepOptions().apply {
            selectedOptions.add(FragmentedSettings.Option("compile.path", true))
        }

        val copied = original.deepCopy()
        copied.selectedOptions[0].visible = false
        copied.selectedOptions.add(FragmentedSettings.Option("compile.args", true))

        assertTrue(original.selectedOptions[0].visible)
        assertEquals(1, original.selectedOptions.size)
        assertEquals(2, copied.selectedOptions.size)
    }

    private fun assertStepFieldsEqual(
        original: LatexStepRunConfigurationOptions,
        copied: LatexStepRunConfigurationOptions,
    ) {
        when {
            original is LatexCompileStepOptions && copied is LatexCompileStepOptions -> {
                assertEquals(original.compiler, copied.compiler)
                assertEquals(original.compilerPath, copied.compilerPath)
                assertEquals(original.compilerArguments, copied.compilerArguments)
                assertEquals(original.outputFormat, copied.outputFormat)
                assertEquals(original.beforeRunCommand, copied.beforeRunCommand)
            }

            original is LatexmkCompileStepOptions && copied is LatexmkCompileStepOptions -> {
                assertEquals(original.compilerPath, copied.compilerPath)
                assertEquals(original.compilerArguments, copied.compilerArguments)
                assertEquals(original.latexmkCompileMode, copied.latexmkCompileMode)
                assertEquals(original.latexmkCustomEngineCommand, copied.latexmkCustomEngineCommand)
                assertEquals(original.latexmkCitationTool, copied.latexmkCitationTool)
                assertEquals(original.latexmkExtraArguments, copied.latexmkExtraArguments)
                assertEquals(original.beforeRunCommand, copied.beforeRunCommand)
            }

            original is PdfViewerStepOptions && copied is PdfViewerStepOptions -> {
                assertEquals(original.pdfViewerName, copied.pdfViewerName)
                assertEquals(original.requireFocus, copied.requireFocus)
                assertEquals(original.customViewerCommand, copied.customViewerCommand)
            }

            original is BibtexStepOptions && copied is BibtexStepOptions -> {
                assertEquals(original.bibliographyCompiler, copied.bibliographyCompiler)
                assertEquals(original.compilerPath, copied.compilerPath)
                assertEquals(original.compilerArguments, copied.compilerArguments)
                assertEquals(original.workingDirectoryPath, copied.workingDirectoryPath)
                assertEquals(original.beforeRunCommand, copied.beforeRunCommand)
            }

            original is MakeindexStepOptions && copied is MakeindexStepOptions -> {
                assertEquals(original.program, copied.program)
                assertEquals(original.commandLineArguments, copied.commandLineArguments)
                assertEquals(original.workingDirectoryPath, copied.workingDirectoryPath)
                assertEquals(original.targetBaseNameOverride, copied.targetBaseNameOverride)
                assertEquals(original.beforeRunCommand, copied.beforeRunCommand)
            }

            original is ExternalToolStepOptions && copied is ExternalToolStepOptions -> {
                assertEquals(original.executable, copied.executable)
                assertEquals(original.arguments, copied.arguments)
                assertEquals(original.workingDirectoryPath, copied.workingDirectoryPath)
                assertEquals(original.beforeRunCommand, copied.beforeRunCommand)
            }

            original is PythontexStepOptions && copied is PythontexStepOptions -> {
                assertEquals(original.executable, copied.executable)
                assertEquals(original.arguments, copied.arguments)
                assertEquals(original.workingDirectoryPath, copied.workingDirectoryPath)
                assertEquals(original.beforeRunCommand, copied.beforeRunCommand)
            }

            original is MakeglossariesStepOptions && copied is MakeglossariesStepOptions -> {
                assertEquals(original.executable, copied.executable)
                assertEquals(original.arguments, copied.arguments)
                assertEquals(original.workingDirectoryPath, copied.workingDirectoryPath)
                assertEquals(original.beforeRunCommand, copied.beforeRunCommand)
            }

            original is XindyStepOptions && copied is XindyStepOptions -> {
                assertEquals(original.executable, copied.executable)
                assertEquals(original.arguments, copied.arguments)
                assertEquals(original.workingDirectoryPath, copied.workingDirectoryPath)
                assertEquals(original.beforeRunCommand, copied.beforeRunCommand)
            }

            else -> fail("Unexpected step type pair: ${original::class.simpleName} / ${copied::class.simpleName}")
        }
    }
}
