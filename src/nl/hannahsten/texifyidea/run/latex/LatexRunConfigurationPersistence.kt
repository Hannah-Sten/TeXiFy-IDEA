package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.configuration.EnvironmentVariablesData
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.run.common.addTextChild
import nl.hannahsten.texifyidea.run.common.getOrCreateAndClearParent
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.util.Log
import org.jdom.Element

internal object LatexRunConfigurationPersistence {

    private const val TEXIFY_PARENT = "texify"
    private const val ATTR_VERSION = "version"
    private const val V2 = "2"

    private const val COMMON = "common"
    private const val MAIN_FILE = "main-file"
    private const val WORKING_DIRECTORY = "working-directory"
    private const val OUTPUT_PATH = "output-path"
    private const val AUXIL_PATH = "auxil-path"
    private const val EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES = "expand-macros-in-environment-variables"
    private const val LATEX_DISTRIBUTION = "latex-distribution"

    private const val STEPS = "steps"
    private const val STEP = "step"
    private const val ATTR_ID = "id"
    private const val ATTR_TYPE = "type"
    private const val ATTR_ENABLED = "enabled"

    private const val COMPILER = "compiler"
    private const val COMPILER_PATH = "compiler-path"
    private const val COMPILER_ARGUMENTS = "compiler-arguments"
    private const val OUTPUT_FORMAT = "output-format"
    private const val BEFORE_RUN_COMMAND = "before-run-command"

    private const val LATEXMK_COMPILE_MODE = "latexmk-compile-mode"
    private const val LATEXMK_CUSTOM_ENGINE_COMMAND = "latexmk-custom-engine-command"
    private const val LATEXMK_CITATION_TOOL = "latexmk-citation-tool"
    private const val LATEXMK_EXTRA_ARGUMENTS = "latexmk-extra-arguments"

    private const val PDF_VIEWER = "pdf-viewer"
    private const val REQUIRE_FOCUS = "require-focus"
    private const val VIEWER_COMMAND = "viewer-command"

    private const val BIBLIOGRAPHY_COMPILER = "bibliography-compiler"
    private const val MAKEINDEX_PROGRAM = "makeindex-program"
    private const val COMMAND_LINE_ARGUMENTS = "command-line-arguments"
    private const val COMMAND_LINE = "command-line"

    private const val UI = "ui"
    private const val STEP_OPTIONS = "step-options"
    private const val ATTR_STEP_ID = "step-id"
    private const val OPTION = "option"

    fun readInto(runConfig: LatexRunConfiguration, element: Element) {
        runConfig.stepSchemaStatus = StepSchemaReadStatus.PARSED
        val parent = element.getChild(TEXIFY_PARENT)
        if (parent == null || parent.getAttributeValue(ATTR_VERSION) != V2) {
            Log.warn("Invalid or missing texify V2 configuration; using default model")
            runConfig.model = LatexRunConfigModel()
            return
        }

        val common = parent.getChild(COMMON)
        val stepsParent = parent.getChild(STEPS)
        val uiParent = parent.getChild(UI)

        val commonSettings = readCommon(common)
        val stepConfigs = readSteps(stepsParent)
        val uiState = readUi(uiParent, stepConfigs)

        runConfig.model = LatexRunConfigModel(
            common = commonSettings,
            steps = if (stepConfigs.isEmpty()) {
                mutableListOf(LatexCompileStepConfig(), PdfViewerStepConfig())
            }
            else {
                stepConfigs
            },
            ui = uiState,
        )
    }

    fun writeFrom(runConfig: LatexRunConfiguration, element: Element) {
        val parent = getOrCreateAndClearParent(element, TEXIFY_PARENT)
        parent.setAttribute(ATTR_VERSION, V2)

        parent.addContent(writeCommon(runConfig.model.common))
        parent.addContent(writeSteps(runConfig.model.steps))

        val ui = writeUi(runConfig.model)
        if (ui != null) {
            parent.addContent(ui)
        }
    }

    private fun readCommon(commonElement: Element?): LatexCommonSettings {
        if (commonElement == null) {
            return LatexCommonSettings()
        }

        return LatexCommonSettings(
            mainFilePath = commonElement.getChildText(MAIN_FILE)?.trim()?.ifEmpty { null },
            workingDirectory = commonElement.getChildText(WORKING_DIRECTORY)
                ?.takeIf { it.isNotBlank() }
                ?.let(::pathOrNull),
            outputPath = commonElement.getChildText(OUTPUT_PATH)
                ?.takeIf { it.isNotBlank() }
                ?.let(::pathOrNull)
                ?: LatexPathResolver.defaultOutputPath,
            auxilPath = commonElement.getChildText(AUXIL_PATH)
                ?.takeIf { it.isNotBlank() }
                ?.let(::pathOrNull)
                ?: LatexPathResolver.defaultAuxilPath,
            environmentVariables = EnvironmentVariablesData.readExternal(commonElement),
            expandMacrosEnvVariables = commonElement.getChildText(EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES)?.toBoolean() ?: false,
            latexDistribution = LatexDistributionType.valueOfIgnoreCase(commonElement.getChildText(LATEX_DISTRIBUTION)),
        )
    }

    private fun writeCommon(common: LatexCommonSettings): Element {
        val commonElement = Element(COMMON)
        commonElement.addTextChild(MAIN_FILE, common.mainFilePath ?: "")
        commonElement.addTextChild(WORKING_DIRECTORY, common.workingDirectory?.toString() ?: "")
        commonElement.addTextChild(OUTPUT_PATH, common.outputPath?.toString() ?: "")
        commonElement.addTextChild(AUXIL_PATH, common.auxilPath?.toString() ?: "")
        common.environmentVariables.writeExternal(commonElement)
        commonElement.addTextChild(EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES, common.expandMacrosEnvVariables.toString())
        commonElement.addTextChild(LATEX_DISTRIBUTION, common.latexDistribution.name)
        return commonElement
    }

    private fun readSteps(stepsParent: Element?): MutableList<LatexStepConfig> {
        if (stepsParent == null) {
            return mutableListOf()
        }

        return stepsParent.getChildren(STEP)
            .mapNotNull(::readStep)
            .toMutableList()
    }

    private fun readStep(stepElement: Element): LatexStepConfig? {
        val type = stepElement.getAttributeValue(ATTR_TYPE)?.trim()?.lowercase()?.takeIf(String::isNotBlank)
            ?: return null
        val id = stepElement.getAttributeValue(ATTR_ID)?.trim()?.takeIf(String::isNotBlank) ?: generateLatexStepId()
        val enabled = stepElement.getAttributeValue(ATTR_ENABLED)?.toBoolean() ?: true

        return when (type) {
            LatexStepType.LATEX_COMPILE -> LatexCompileStepConfig(
                id = id,
                enabled = enabled,
                compiler = stepElement.getChildText(COMPILER)
                    ?.let { runCatching { LatexCompiler.valueOf(it) }.getOrNull() }
                    ?.takeIf { it != LatexCompiler.LATEXMK }
                    ?: LatexCompiler.PDFLATEX,
                compilerPath = stepElement.getChildText(COMPILER_PATH)?.ifBlank { null },
                compilerArguments = stepElement.getChildText(COMPILER_ARGUMENTS)?.ifBlank { null },
                outputFormat = Format.byNameIgnoreCase(stepElement.getChildText(OUTPUT_FORMAT)),
                beforeRunCommand = stepElement.getChildText(BEFORE_RUN_COMMAND)?.ifBlank { null },
            )

            LatexStepType.LATEXMK_COMPILE -> LatexmkCompileStepConfig(
                id = id,
                enabled = enabled,
                compilerPath = stepElement.getChildText(COMPILER_PATH)?.ifBlank { null },
                compilerArguments = stepElement.getChildText(COMPILER_ARGUMENTS)?.ifBlank { null },
                latexmkCompileMode = stepElement.getChildText(LATEXMK_COMPILE_MODE)
                    ?.let { runCatching { LatexmkCompileMode.valueOf(it) }.getOrNull() }
                    ?: LatexmkCompileMode.AUTO,
                latexmkCustomEngineCommand = stepElement.getChildText(LATEXMK_CUSTOM_ENGINE_COMMAND)?.ifBlank { null },
                latexmkCitationTool = stepElement.getChildText(LATEXMK_CITATION_TOOL)
                    ?.let { runCatching { LatexmkCitationTool.valueOf(it) }.getOrNull() }
                    ?: LatexmkCitationTool.AUTO,
                latexmkExtraArguments = stepElement.getChildText(LATEXMK_EXTRA_ARGUMENTS)?.ifBlank { null }
                    ?: LatexRunConfiguration.DEFAULT_LATEXMK_EXTRA_ARGUMENTS,
                beforeRunCommand = stepElement.getChildText(BEFORE_RUN_COMMAND)?.ifBlank { null },
            )

            LatexStepType.PDF_VIEWER -> PdfViewerStepConfig(
                id = id,
                enabled = enabled,
                pdfViewerName = stepElement.getChildText(PDF_VIEWER)?.ifBlank { null },
                requireFocus = stepElement.getChildText(REQUIRE_FOCUS)?.toBoolean() ?: true,
                customViewerCommand = stepElement.getChildText(VIEWER_COMMAND)?.ifBlank { null },
            )

            LatexStepType.BIBTEX -> BibtexStepConfig(
                id = id,
                enabled = enabled,
                bibliographyCompiler = stepElement.getChildText(BIBLIOGRAPHY_COMPILER)
                    ?.let { runCatching { BibliographyCompiler.valueOf(it) }.getOrNull() }
                    ?: BibliographyCompiler.BIBTEX,
                compilerPath = stepElement.getChildText(COMPILER_PATH)?.ifBlank { null },
                compilerArguments = stepElement.getChildText(COMPILER_ARGUMENTS)?.ifBlank { null },
            )

            LatexStepType.MAKEINDEX -> MakeindexStepConfig(
                id = id,
                enabled = enabled,
                program = stepElement.getChildText(MAKEINDEX_PROGRAM)
                    ?.let { runCatching { MakeindexProgram.valueOf(it) }.getOrNull() }
                    ?: MakeindexProgram.MAKEINDEX,
                commandLineArguments = stepElement.getChildText(COMMAND_LINE_ARGUMENTS)?.ifBlank { null },
            )

            LatexStepType.EXTERNAL_TOOL -> ExternalToolStepConfig(
                id = id,
                enabled = enabled,
                commandLine = stepElement.getChildText(COMMAND_LINE)?.ifBlank { null },
            )

            LatexStepType.PYTHONTEX -> PythontexStepConfig(
                id = id,
                enabled = enabled,
                commandLine = stepElement.getChildText(COMMAND_LINE)?.ifBlank { null },
            )

            LatexStepType.MAKEGLOSSARIES -> MakeglossariesStepConfig(
                id = id,
                enabled = enabled,
                commandLine = stepElement.getChildText(COMMAND_LINE)?.ifBlank { null },
            )

            LatexStepType.XINDY -> XindyStepConfig(
                id = id,
                enabled = enabled,
                commandLine = stepElement.getChildText(COMMAND_LINE)?.ifBlank { null },
            )

            else -> null
        }
    }

    private fun writeSteps(steps: List<LatexStepConfig>): Element {
        val stepsElement = Element(STEPS)
        val effectiveSteps = if (steps.isEmpty()) {
            listOf(LatexCompileStepConfig(), PdfViewerStepConfig())
        }
        else {
            steps
        }

        effectiveSteps.forEach { step ->
            val element = Element(STEP)
                .setAttribute(ATTR_ID, step.id)
                .setAttribute(ATTR_TYPE, step.type)
                .setAttribute(ATTR_ENABLED, step.enabled.toString())

            when (step) {
                is LatexCompileStepConfig -> {
                    element.addTextChild(COMPILER, step.compiler.name)
                    element.addTextChild(COMPILER_PATH, step.compilerPath ?: "")
                    element.addTextChild(COMPILER_ARGUMENTS, step.compilerArguments ?: "")
                    element.addTextChild(OUTPUT_FORMAT, step.outputFormat.name)
                    element.addTextChild(BEFORE_RUN_COMMAND, step.beforeRunCommand ?: "")
                }

                is LatexmkCompileStepConfig -> {
                    element.addTextChild(COMPILER_PATH, step.compilerPath ?: "")
                    element.addTextChild(COMPILER_ARGUMENTS, step.compilerArguments ?: "")
                    element.addTextChild(LATEXMK_COMPILE_MODE, step.latexmkCompileMode.name)
                    element.addTextChild(LATEXMK_CUSTOM_ENGINE_COMMAND, step.latexmkCustomEngineCommand ?: "")
                    element.addTextChild(LATEXMK_CITATION_TOOL, step.latexmkCitationTool.name)
                    element.addTextChild(LATEXMK_EXTRA_ARGUMENTS, step.latexmkExtraArguments ?: "")
                    element.addTextChild(BEFORE_RUN_COMMAND, step.beforeRunCommand ?: "")
                }

                is PdfViewerStepConfig -> {
                    element.addTextChild(PDF_VIEWER, step.pdfViewerName ?: "")
                    element.addTextChild(REQUIRE_FOCUS, step.requireFocus.toString())
                    element.addTextChild(VIEWER_COMMAND, step.customViewerCommand ?: "")
                }

                is BibtexStepConfig -> {
                    element.addTextChild(BIBLIOGRAPHY_COMPILER, step.bibliographyCompiler.name)
                    element.addTextChild(COMPILER_PATH, step.compilerPath ?: "")
                    element.addTextChild(COMPILER_ARGUMENTS, step.compilerArguments ?: "")
                }

                is MakeindexStepConfig -> {
                    element.addTextChild(MAKEINDEX_PROGRAM, step.program.name)
                    element.addTextChild(COMMAND_LINE_ARGUMENTS, step.commandLineArguments ?: "")
                }

                is ExternalToolStepConfig -> element.addTextChild(COMMAND_LINE, step.commandLine ?: "")
                is PythontexStepConfig -> element.addTextChild(COMMAND_LINE, step.commandLine ?: "")
                is MakeglossariesStepConfig -> element.addTextChild(COMMAND_LINE, step.commandLine ?: "")
                is XindyStepConfig -> element.addTextChild(COMMAND_LINE, step.commandLine ?: "")
            }

            stepsElement.addContent(element)
        }

        return stepsElement
    }

    private fun readUi(uiParent: Element?, steps: List<LatexStepConfig>): LatexUiState {
        if (uiParent == null) {
            return LatexUiState()
        }

        val stepIdSet = steps.map { it.id }.toSet()
        val byStep = mutableMapOf<String, MutableSet<String>>()

        for (stepOptions in uiParent.getChildren(STEP_OPTIONS)) {
            val stepId = stepOptions.getAttributeValue(ATTR_STEP_ID)?.trim()?.takeIf(String::isNotBlank) ?: continue
            if (stepId !in stepIdSet) {
                continue
            }
            val optionIds = stepOptions.getChildren(OPTION)
                .mapNotNull { it.getAttributeValue(ATTR_ID)?.trim()?.takeIf(String::isNotBlank) }
                .toMutableSet()
            if (optionIds.isNotEmpty()) {
                byStep[stepId] = optionIds
            }
        }

        return LatexUiState(stepUiOptionIdsByStepId = byStep)
    }

    private fun writeUi(model: LatexRunConfigModel): Element? {
        if (model.ui.stepUiOptionIdsByStepId.isEmpty()) {
            return null
        }

        val stepIds = model.steps.map { it.id }.toSet()
        val ui = Element(UI)

        model.ui.stepUiOptionIdsByStepId.toSortedMap().forEach { (stepId, optionIds) ->
            if (stepId !in stepIds || optionIds.isEmpty()) {
                return@forEach
            }
            val stepOptions = Element(STEP_OPTIONS).setAttribute(ATTR_STEP_ID, stepId)
            optionIds.toSortedSet().forEach { optionId ->
                stepOptions.addContent(Element(OPTION).setAttribute(ATTR_ID, optionId))
            }
            ui.addContent(stepOptions)
        }

        return if (ui.getChildren(STEP_OPTIONS).isEmpty()) {
            null
        }
        else {
            ui
        }
    }
}
