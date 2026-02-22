package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.run.common.addTextChild
import nl.hannahsten.texifyidea.run.common.getOrCreateAndClearParent
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.SumatraViewer
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.runInBackgroundNonBlocking
import org.jdom.Element
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal object LatexRunConfigurationPersistence {

    private const val TEXIFY_PARENT = "texify"
    private const val COMPILER = "compiler"
    private const val COMPILER_PATH = "compiler-path"
    private const val SUMATRA_PATH = "sumatra-path"
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
    private const val EXTERNAL_TOOL_RUN_CONFIG = "external-tool-run-config"
    private const val BIB_RUN_CONFIGS = "bib-run-configs"
    private const val MAKEINDEX_RUN_CONFIGS = "makeindex-run-configs"
    private const val EXTERNAL_TOOL_RUN_CONFIGS = "external-tool-run-configs"
    private const val EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES = "expand-macros-in-environment-variables"
    private const val LATEXMK_COMPILE_MODE = "latexmk-compile-mode"
    private const val LATEXMK_CUSTOM_ENGINE_COMMAND = "latexmk-custom-engine-command"
    private const val LATEXMK_CITATION_TOOL = "latexmk-citation-tool"
    private const val LATEXMK_EXTRA_ARGUMENTS = "latexmk-extra-arguments"

    fun readInto(runConfig: LatexRunConfiguration, element: Element) {
        runConfig.stepSchemaStatus = StepSchemaReadStatus.MISSING
        runConfig.stepSchemaTypes = emptyList()
        val parent = element.getChild(TEXIFY_PARENT) ?: return
        val schemaStatus = LatexRunConfigurationSerializer.probeStepSchema(parent)

        runConfig.compiler = parent.getChildText(COMPILER)
            ?.let { runCatching { LatexCompiler.valueOf(it) }.getOrNull() }
        runConfig.compilerPath = parent.getChildText(COMPILER_PATH)?.ifBlank { null }
        val viewerName = parent.getChildText(PDF_VIEWER)
        runConfig.pdfViewer = PdfViewer.availableViewers.firstOrNull { it.name == viewerName || it.name?.uppercase() == viewerName }
            ?: PdfViewer.firstAvailableViewer
        runConfig.requireFocus = parent.getChildText(REQUIRE_FOCUS)?.toBoolean() ?: true
        runConfig.viewerCommand = parent.getChildText(VIEWER_COMMAND)?.ifBlank { null }

        parent.getChildText(SUMATRA_PATH)?.let { folder ->
            migrateSumatraPath(runConfig, folder)
        }

        runConfig.compilerArguments = parent.getChildText(COMPILER_ARGUMENTS)?.ifBlank { null }
        runConfig.environmentVariables = EnvironmentVariablesData.readExternal(parent)
        runConfig.expandMacrosEnvVariables = parent.getChildText(EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES)?.toBoolean() ?: false
        runConfig.beforeRunCommand = parent.getChildText(BEFORE_RUN_COMMAND)?.ifBlank { null }
        runConfig.mainFilePath = parent.getChildText(MAIN_FILE)

        runConfig.outputPath = parent.getChildText(OUTPUT_PATH)?.let { outputPathString ->
            if (isInvalidJetBrainsBinPath(outputPathString)) LatexPathResolver.defaultOutputPath else pathOrNull(outputPathString)
        } ?: runConfig.outputPath

        runConfig.auxilPath = parent.getChildText(AUXIL_PATH)?.let { pathOrNull(it) } ?: runConfig.auxilPath

        runConfig.workingDirectory = parent.getChildText(WORKING_DIRECTORY)?.let { text ->
            when {
                text.isBlank() -> null
                text == LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER -> null
                else -> pathOrNull(text)
            }
        }

        runConfig.compileTwice = parent.getChildText(COMPILE_TWICE)?.toBoolean() ?: false
        if (runConfig.compiler == LatexCompiler.LATEXMK) {
            runConfig.compileTwice = false
        }
        runConfig.outputFormat = Format.byNameIgnoreCase(parent.getChildText(OUTPUT_FORMAT))
        runConfig.latexDistribution = LatexDistributionType.valueOfIgnoreCase(parent.getChildText(LATEX_DISTRIBUTION))

        runConfig.latexmkCompileMode = parent.getChildText(LATEXMK_COMPILE_MODE)
            ?.let { runCatching { LatexmkCompileMode.valueOf(it) }.getOrNull() }
            ?: LatexmkCompileMode.AUTO
        runConfig.latexmkCustomEngineCommand = parent.getChildText(LATEXMK_CUSTOM_ENGINE_COMMAND)
        runConfig.latexmkCitationTool = parent.getChildText(LATEXMK_CITATION_TOOL)
            ?.let { runCatching { LatexmkCitationTool.valueOf(it) }.getOrNull() }
            ?: LatexmkCitationTool.AUTO
        runConfig.latexmkExtraArguments = parent.getChildText(LATEXMK_EXTRA_ARGUMENTS) ?: LatexRunConfiguration.DEFAULT_LATEXMK_EXTRA_ARGUMENTS

        runConfig.setAuxRunConfigIds(LatexRunConfigurationSerializer.readRunConfigIds(parent, BIB_RUN_CONFIGS, BIB_RUN_CONFIG))
        runConfig.setMakeindexRunConfigIds(LatexRunConfigurationSerializer.readRunConfigIds(parent, MAKEINDEX_RUN_CONFIGS, MAKEINDEX_RUN_CONFIG))
        runConfig.setExternalToolRunConfigIds(LatexRunConfigurationSerializer.readRunConfigIds(parent, EXTERNAL_TOOL_RUN_CONFIGS, EXTERNAL_TOOL_RUN_CONFIG))

        runConfig.stepSchemaStatus = schemaStatus
        runConfig.stepSchemaTypes = when (schemaStatus) {
            StepSchemaReadStatus.PARSED -> LatexRunConfigurationSerializer.readStepTypes(parent)
            StepSchemaReadStatus.MISSING -> {
                runConfig.stepSchemaStatus = StepSchemaReadStatus.PARSED
                inferStepTypesFromLegacyConfiguration(runConfig)
            }
            StepSchemaReadStatus.INVALID -> inferStepTypesFromLegacyConfiguration(runConfig)
        }
    }

    fun writeFrom(runConfig: LatexRunConfiguration, element: Element) {
        val parent = getOrCreateAndClearParent(element, TEXIFY_PARENT)

        parent.addTextChild(COMPILER, runConfig.compiler?.name ?: "")
        parent.addTextChild(COMPILER_ARGUMENTS, runConfig.compilerArguments ?: "")
        parent.addTextChild(COMPILER_PATH, runConfig.compilerPath ?: "")
        parent.addTextChild(PDF_VIEWER, runConfig.pdfViewer?.name ?: "")
        parent.addTextChild(REQUIRE_FOCUS, runConfig.requireFocus.toString())
        parent.addTextChild(VIEWER_COMMAND, runConfig.viewerCommand ?: "")
        runConfig.environmentVariables.writeExternal(parent)
        parent.addTextChild(EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES, runConfig.expandMacrosEnvVariables.toString())
        parent.addTextChild(BEFORE_RUN_COMMAND, runConfig.beforeRunCommand ?: "")
        parent.addTextChild(MAIN_FILE, runConfig.mainFilePath ?: "")
        parent.addTextChild(WORKING_DIRECTORY, runConfig.workingDirectory?.toString() ?: LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER)
        parent.addTextChild(LATEX_DISTRIBUTION, runConfig.latexDistribution.name)

        parent.addTextChild(OUTPUT_PATH, runConfig.outputPath?.toString() ?: "")
        parent.addTextChild(AUXIL_PATH, runConfig.auxilPath?.toString() ?: "")
        parent.addTextChild(COMPILE_TWICE, runConfig.compileTwice.toString())
        parent.addTextChild(OUTPUT_FORMAT, runConfig.outputFormat.name)

        parent.addTextChild(LATEXMK_COMPILE_MODE, runConfig.latexmkCompileMode.name)
        parent.addTextChild(LATEXMK_CUSTOM_ENGINE_COMMAND, runConfig.latexmkCustomEngineCommand ?: "")
        parent.addTextChild(LATEXMK_CITATION_TOOL, runConfig.latexmkCitationTool.name)
        parent.addTextChild(LATEXMK_EXTRA_ARGUMENTS, runConfig.latexmkExtraArguments ?: "")

        LatexRunConfigurationSerializer.writeRunConfigIds(parent, BIB_RUN_CONFIGS, runConfig.getBibRunConfigIds())
        LatexRunConfigurationSerializer.writeRunConfigIds(parent, MAKEINDEX_RUN_CONFIGS, runConfig.getMakeindexRunConfigIds())
        LatexRunConfigurationSerializer.writeRunConfigIds(parent, EXTERNAL_TOOL_RUN_CONFIGS, runConfig.getExternalToolRunConfigIds())

        val stepTypes = runConfig.stepSchemaTypes.ifEmpty {
            inferStepTypesFromLegacyConfiguration(runConfig)
        }
        LatexRunConfigurationSerializer.writeStepTypes(parent, stepTypes)
    }

    private fun inferStepTypesFromLegacyConfiguration(runConfig: LatexRunConfiguration): List<String> {
        val inferred = mutableListOf<String>()
        if (runConfig.compiler != null) {
            inferred += "latex-compile"
        }
        if (runConfig.pdfViewer != null || !runConfig.viewerCommand.isNullOrBlank()) {
            inferred += "pdf-viewer"
        }
        return inferred
    }

    private fun migrateSumatraPath(runConfig: LatexRunConfiguration, folder: String) {
        runInBackgroundNonBlocking(runConfig.project, "Set SumatraPDF path") {
            val settings = TexifySettings.getState()
            val isSumatraPathSet = readAction { settings.pathToSumatra != null }
            if (isSumatraPathSet) {
                return@runInBackgroundNonBlocking
            }
            val path = try {
                Path.of(folder).resolve("SumatraPDF.exe")
            }
            catch (_: InvalidPathException) {
                return@runInBackgroundNonBlocking
            }
            if (SumatraViewer.trySumatraPath(path)) {
                @Suppress("UnstableApiUsage")
                writeAction {
                    TexifySettings.getState().pathToSumatra = path.absolutePathString()
                }
            }
        }
    }
}
