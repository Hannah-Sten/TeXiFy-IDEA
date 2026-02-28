package nl.hannahsten.texifyidea.run

import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.ui.FragmentedSettings
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.run.latex.*
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.makeindex.MakeindexRunConfiguration
import nl.hannahsten.texifyidea.run.makeindex.MakeindexRunConfigurationType
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import org.jdom.Element
import org.jdom.Namespace
import java.nio.file.Files

class LatexRunConfigurationTest : BasePlatformTestCase() {

    fun testConfigurationEditorUsesFragmentedSettingsEditor() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        val editor = runConfig.configurationEditor
        assertTrue(editor is LatexSettingsEditor)
    }

    fun testWriteReadRoundTripPreservesCommonAndSteps() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.mainFilePath = "main.tex"
        runConfig.workingDirectory = java.nio.file.Path.of("{mainFileParent}")
        runConfig.outputPath = java.nio.file.Path.of("{projectDir}/out")
        runConfig.auxilPath = java.nio.file.Path.of("{projectDir}/aux")
        runConfig.latexDistribution = LatexDistributionType.TEXLIVE

        runConfig.configOptions.steps = mutableListOf(
            LatexmkCompileStepOptions().apply {
                id = "compile-1"
                compilerPath = "/usr/bin/latexmk"
                compilerArguments = "-shell-escape"
                latexmkCompileMode = LatexmkCompileMode.CUSTOM
                latexmkCustomEngineCommand = "xelatex"
            },
            PdfViewerStepOptions().apply {
                id = "viewer-1"
                customViewerCommand = "open {pdf}"
                requireFocus = false
            }
        )

        val element = Element("configuration", Namespace.getNamespace("", ""))
        runConfig.writeExternal(element)

        val restored = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Restored")
        restored.readExternal(element)

        assertEquals("main.tex", restored.mainFilePath)
        assertEquals("{projectDir}/out", restored.outputPath.toString())
        assertEquals("{projectDir}/aux", restored.auxilPath.toString())
        assertEquals(LatexDistributionType.TEXLIVE, restored.latexDistribution)
        assertEquals(2, restored.configOptions.steps.size)
        assertEquals("latexmk-compile", restored.configOptions.steps[0].type)
        assertEquals("pdf-viewer", restored.configOptions.steps[1].type)
        assertEquals("compile-1", restored.configOptions.steps[0].id)
        assertEquals("viewer-1", restored.configOptions.steps[1].id)
    }

    fun testEmptyStepListFallsBackToDefaultSteps() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.configOptions.steps = mutableListOf()

        runConfig.configOptions.ensureDefaultSteps()

        assertEquals(listOf(LatexStepType.LATEXMK_COMPILE, LatexStepType.PDF_VIEWER), runConfig.configOptions.steps.map { it.type })
    }

    fun testTemplateConfigurationAppliesAutoCompletionOnCreation() {
        val template = LatexRunConfigurationProducer().configurationFactory
            .createTemplateConfiguration(myFixture.project) as LatexRunConfiguration

        assertEquals(
            listOf(LatexStepType.LATEXMK_COMPILE, LatexStepType.PDF_VIEWER),
            template.configOptions.steps.map { it.type }
        )
    }

    fun testWriteReadRoundTripPreservesFileCleanupStep() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.mainFilePath = "main.tex"
        runConfig.configOptions.steps = mutableListOf(
            LatexCompileStepOptions().apply { id = "compile-1" },
            FileCleanupStepOptions().apply { id = "cleanup-1" },
            PdfViewerStepOptions().apply { id = "viewer-1" },
        )

        val element = Element("configuration", Namespace.getNamespace("", ""))
        runConfig.writeExternal(element)

        val restored = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Restored")
        restored.readExternal(element)

        assertEquals(listOf("compile-1", "cleanup-1", "viewer-1"), restored.configOptions.steps.map { it.id })
        assertEquals(
            listOf(LatexStepType.LATEX_COMPILE, LatexStepType.FILE_CLEANUP, LatexStepType.PDF_VIEWER),
            restored.configOptions.steps.map { it.type }
        )
    }

    fun testCloneDeepCopiesStepsAndEnvironmentVariables() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.mainFilePath = "main.tex"
        runConfig.configOptions.environmentVariables = mutableListOf(
            LatexRunConfigurationOptions.EnvironmentVariableEntry().apply {
                name = "BIBINPUTS"
                value = "/tmp/bib"
            }
        )
        runConfig.configOptions.steps = mutableListOf(
            LatexCompileStepOptions().apply {
                id = "compile-1"
                compilerArguments = "-shell-escape"
                selectedOptions.add(FragmentedSettings.Option(StepUiOptionIds.COMPILE_ARGS, true))
            }
        )

        val cloned = runConfig.clone() as LatexRunConfiguration
        val originalStep = runConfig.configOptions.steps.single()
        val clonedStep = cloned.configOptions.steps.single()
        val originalEnv = runConfig.configOptions.environmentVariables.single()
        val clonedEnv = cloned.configOptions.environmentVariables.single()

        assertEquals("main.tex", cloned.mainFilePath)
        assertNotSame(runConfig.configOptions.steps, cloned.configOptions.steps)
        assertNotSame(originalStep, clonedStep)
        assertEquals(originalStep.type, clonedStep.type)
        assertNotSame(originalStep.selectedOptions, clonedStep.selectedOptions)
        assertEquals(originalStep.selectedOptions.single().name, clonedStep.selectedOptions.single().name)
        assertNotSame(originalStep.selectedOptions.single(), clonedStep.selectedOptions.single())
        assertNotSame(runConfig.configOptions.environmentVariables, cloned.configOptions.environmentVariables)
        assertNotSame(originalEnv, clonedEnv)
        assertEquals(originalEnv.name, clonedEnv.name)
        assertEquals(originalEnv.value, clonedEnv.value)
    }

    fun testFragmentedEditorUsesCommonSequenceAndStepSettingsLayout() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        val editor = LatexSettingsEditor(runConfig)
        val createRunFragments = LatexSettingsEditor::class.java.getDeclaredMethod("createRunFragments")
        createRunFragments.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val fragments = createRunFragments.invoke(editor) as MutableList<com.intellij.execution.ui.SettingsEditorFragment<LatexRunConfiguration, *>>
        val ids = fragments.mapNotNull { fragment ->
            runCatching {
                fragment.javaClass.getMethod("getId").invoke(fragment) as? String
            }.getOrNull()
        }

        assertTrue(ids.contains("mainFile"))
        assertTrue(ids.contains("latexDistribution"))
        assertTrue(ids.contains("workingDirectory"))
        assertTrue(ids.contains("outputDirectory"))
        assertTrue(ids.contains("auxiliaryDirectory"))
        assertTrue(ids.contains("environmentVariables"))
        assertTrue(ids.contains("compileSequence"))
        assertTrue(ids.contains("stepSettings"))
        assertTrue(ids.indexOf("mainFile") < ids.indexOf("compileSequence"))
        assertTrue(ids.indexOf("compileSequence") < ids.indexOf("stepSettings"))
        assertFalse(ids.contains("legacyAdvancedOptions"))
    }

    fun testLegacyClassicConfigMigratesToCompileAndViewer() {
        val legacyViewerName = (PdfViewer.firstAvailableViewer.name ?: PdfViewer.availableViewers.firstNotNullOfOrNull { it.name } ?: "unknown").uppercase()
        val element = legacyConfigurationElement(
            MAIN_FILE to "legacy/main.tex",
            COMPILER to "PDFLATEX",
            COMPILER_PATH to "/usr/bin/pdflatex",
            COMPILER_ARGUMENTS to "-shell-escape",
            BEFORE_RUN_COMMAND to "\\scrollmode",
            OUTPUT_FORMAT to "PDF",
            PDF_VIEWER to legacyViewerName,
            REQUIRE_FOCUS to "false",
            VIEWER_COMMAND to "open {pdf}",
            LATEX_DISTRIBUTION to "TEXLIVE",
            EXPAND_MACROS to "true",
            WORKING_DIRECTORY to "{mainFileParent}",
            OUTPUT_PATH to "{projectDir}/out",
            AUXIL_PATH to "{projectDir}/auxil",
        )

        val restored = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Restored")
        restored.readExternal(element)

        assertEquals("legacy/main.tex", restored.mainFilePath)
        assertEquals(LatexDistributionType.TEXLIVE, restored.latexDistribution)
        assertTrue(restored.expandMacrosEnvVariables)
        assertNull(restored.workingDirectory)
        assertEquals("{projectDir}/out", restored.outputPath.toString())
        assertEquals("{projectDir}/auxil", restored.auxilPath.toString())

        val compile = restored.configOptions.steps.filterIsInstance<LatexCompileStepOptions>().first()
        assertEquals(nl.hannahsten.texifyidea.run.compiler.LatexCompiler.PDFLATEX, compile.compiler)
        assertEquals("/usr/bin/pdflatex", compile.compilerPath)
        assertEquals("-shell-escape", compile.compilerArguments)
        assertEquals("\\scrollmode", compile.beforeRunCommand)
        assertEquals(nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format.PDF, compile.outputFormat)

        val viewer = restored.configOptions.steps.filterIsInstance<PdfViewerStepOptions>().single()
        assertEquals(PdfViewer.firstAvailableViewer.name, viewer.pdfViewerName)
        assertFalse(viewer.requireFocus)
        assertEquals("open {pdf}", viewer.customViewerCommand)
    }

    fun testLegacyLatexmkOutputFormatMapsToCompileMode() {
        val element = legacyConfigurationElement(
            COMPILER to "LATEXMK",
            OUTPUT_FORMAT to "DVI",
        )

        val restored = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Restored")
        restored.readExternal(element)

        val compile = restored.configOptions.steps.filterIsInstance<LatexmkCompileStepOptions>().first()
        assertEquals(LatexmkCompileMode.LATEX_DVI, compile.latexmkCompileMode)
    }

    fun testLegacyCompileTwiceAddsExtraClassicCompileWhenNoAux() {
        val element = legacyConfigurationElement(
            COMPILER to "PDFLATEX",
            COMPILE_TWICE to "true",
        )

        val restored = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Restored")
        restored.readExternal(element)

        assertEquals(
            listOf(LatexStepType.LATEX_COMPILE, LatexStepType.LATEX_COMPILE, LatexStepType.PDF_VIEWER),
            restored.configOptions.steps.map { it.type }
        )
    }

    fun testLegacyBibAndMakeindexIdsMigrateToSteps() {
        val runManager = RunManagerImpl.getInstanceImpl(project)
        val workDir = Files.createTempDirectory("legacy-aux").toString()
        val workDirVf = requireNotNull(LocalFileSystem.getInstance().refreshAndFindFileByPath(workDir))

        val bibSettings = runManager.createConfiguration(
            "",
            LatexConfigurationFactory(BibtexRunConfigurationType())
        )
        val bibConfig = bibSettings.configuration as BibtexRunConfiguration
        bibConfig.compiler = BibliographyCompiler.BIBER
        bibConfig.compilerPath = "/usr/bin/biber"
        bibConfig.compilerArguments = "--quiet"
        bibConfig.bibWorkingDir = workDirVf
        runManager.addConfiguration(bibSettings)

        val makeindexSettings = runManager.createConfiguration(
            "",
            LatexConfigurationFactory(MakeindexRunConfigurationType())
        )
        val makeindexConfig = makeindexSettings.configuration as MakeindexRunConfiguration
        makeindexConfig.makeindexProgram = MakeindexProgram.XINDY
        makeindexConfig.commandLineArguments = "-L english"
        makeindexConfig.workingDirectory = workDirVf
        runManager.addConfiguration(makeindexSettings)

        val element = legacyConfigurationElement(
            COMPILER to "PDFLATEX",
            BIB_RUN_CONFIG to "[${bibSettings.uniqueID}]",
            MAKEINDEX_RUN_CONFIG to "[${makeindexSettings.uniqueID}]",
        )

        val restored = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Restored")
        restored.readExternal(element)

        val bibStep = restored.configOptions.steps.filterIsInstance<BibtexStepOptions>().single()
        assertEquals(BibliographyCompiler.BIBER, bibStep.bibliographyCompiler)
        assertEquals("/usr/bin/biber", bibStep.compilerPath)
        assertEquals("--quiet", bibStep.compilerArguments)
        assertEquals(workDirVf.path, bibStep.workingDirectoryPath)

        val makeindexStep = restored.configOptions.steps.filterIsInstance<MakeindexStepOptions>().single()
        assertEquals(MakeindexProgram.XINDY, makeindexStep.program)
        assertEquals("-L english", makeindexStep.commandLineArguments)
        assertEquals(workDirVf.path, makeindexStep.workingDirectoryPath)
    }

    fun testLegacyDanglingBibAndMakeindexIdsAppendDefaultSteps() {
        val element = legacyConfigurationElement(
            COMPILER to "PDFLATEX",
            BIB_RUN_CONFIG to "[missing-bib-id]",
            MAKEINDEX_RUN_CONFIG to "[missing-makeindex-id]",
        )

        val restored = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Restored")
        restored.readExternal(element)

        assertEquals(1, restored.configOptions.steps.count { it is BibtexStepOptions })
        assertEquals(1, restored.configOptions.steps.count { it is MakeindexStepOptions })
    }

    fun testLegacyOutAuxBooleanFallbackMappingWorks() {
        val element = legacyConfigurationElement(
            COMPILER to "PDFLATEX",
            OUT_DIR to "true",
            AUX_DIR to "false",
        )

        val restored = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Restored")
        restored.readExternal(element)

        assertEquals("{projectDir}/out", restored.outputPath.toString())
        assertEquals("{mainFileParent}", restored.auxilPath.toString())
    }

    fun testNewSchemaPrioritySkipsLegacyMigrationWhenStepsPresent() {
        val source = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Source")
        source.mainFilePath = "from-new-schema.tex"
        source.configOptions.steps = mutableListOf(
            LatexmkCompileStepOptions().apply { id = "new-compile" },
            PdfViewerStepOptions().apply { id = "new-viewer" },
        )
        val element = Element("configuration", Namespace.getNamespace("", ""))
        source.writeExternal(element)

        val legacyParent = Element("texify")
        legacyParent.addContent(Element(COMPILER).apply { text = "PDFLATEX" })
        legacyParent.addContent(Element(MAIN_FILE).apply { text = "from-legacy.tex" })
        element.addContent(legacyParent)

        val restored = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Restored")
        restored.readExternal(element)

        assertEquals("from-new-schema.tex", restored.mainFilePath)
        assertEquals(listOf(LatexStepType.LATEXMK_COMPILE, LatexStepType.PDF_VIEWER), restored.configOptions.steps.map { it.type })
        assertEquals(listOf("new-compile", "new-viewer"), restored.configOptions.steps.map { it.id })
    }

    private fun legacyConfigurationElement(vararg fields: Pair<String, String>): Element {
        val element = Element("configuration", Namespace.getNamespace("", ""))
        val parent = Element("texify")
        fields.forEach { (name, value) ->
            parent.addContent(Element(name).apply { text = value })
        }
        element.addContent(parent)
        return element
    }

    companion object {

        private const val COMPILER = "compiler"
        private const val COMPILER_PATH = "compiler-path"
        private const val PDF_VIEWER = "pdf-viewer"
        private const val REQUIRE_FOCUS = "require-focus"
        private const val VIEWER_COMMAND = "viewer-command"
        private const val COMPILER_ARGUMENTS = "compiler-arguments"
        private const val BEFORE_RUN_COMMAND = "before-run-command"
        private const val MAIN_FILE = "main-file"
        private const val OUTPUT_PATH = "output-path"
        private const val AUXIL_PATH = "auxil-path"
        private const val WORKING_DIRECTORY = "working-directory"
        private const val COMPILE_TWICE = "compile-twice"
        private const val OUTPUT_FORMAT = "output-format"
        private const val LATEX_DISTRIBUTION = "latex-distribution"
        private const val BIB_RUN_CONFIG = "bib-run-config"
        private const val MAKEINDEX_RUN_CONFIG = "makeindex-run-config"
        private const val EXPAND_MACROS = "expand-macros-in-environment-variables"
        private const val AUX_DIR = "aux-dir"
        private const val OUT_DIR = "out-dir"
    }
}
