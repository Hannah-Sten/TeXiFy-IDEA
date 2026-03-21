package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.configuration.EnvironmentVariablesData
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.step.LatexStepAutoConfigurator
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import org.jdom.Element
import java.nio.file.Path
import java.util.Locale

/**
 * Migrates legacy LaTeX run-configuration XML into the current step-based schema.
 * It only runs during deserialization when new-format step options are not explicitly present.
 */
internal object LegacyLatexRunConfigMigration {

    private const val LEGACY_PARENT = "texify"

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
    private const val EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES = "expand-macros-in-environment-variables"
    private const val AUX_DIR = "aux-dir"
    private const val OUT_DIR = "out-dir"

    private const val LEGACY_LATEXMK = "LATEXMK"

    fun migrateIfNeeded(runConfig: LatexRunConfiguration, rootElement: Element) {
        val legacyParent = rootElement.getChild(LEGACY_PARENT) ?: return
        migrate(runConfig, legacyParent)
    }

    private fun migrate(runConfig: LatexRunConfiguration, legacyParent: Element) {
        migrateCommonOptions(runConfig, legacyParent)

        val stepBundle = buildStepBundle(legacyParent)
        val completed = LatexStepAutoConfigurator.completeSteps(
            mainPsiFile = null,
            baseSteps = stepBundle.baseSteps,
        )
        runConfig.configOptions.steps = completed.toMutableList()
    }

    private fun migrateCommonOptions(runConfig: LatexRunConfiguration, legacyParent: Element) {
        legacyParent.getChildTextTrimOrNull(MAIN_FILE)?.let { runConfig.mainFilePath = it }

        parseDistribution(legacyParent.getChildTextTrimOrNull(LATEX_DISTRIBUTION))
            ?.let { runConfig.latexDistribution = it }

        legacyParent.getChildTextTrimOrNull(EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES)
            ?.toBooleanStrictOrNullCompat()
            ?.let { runConfig.expandMacrosEnvVariables = it }

        runCatching {
            runConfig.environmentVariables = EnvironmentVariablesData.readExternal(legacyParent)
        }

        val outputPathText = legacyParent.getChildTextTrimOrNull(OUTPUT_PATH)
        val auxPathText = legacyParent.getChildTextTrimOrNull(AUXIL_PATH)
        val outDirFlag = legacyParent.getChildTextTrimOrNull(OUT_DIR)?.toBooleanStrictOrNullCompat()
        val auxDirFlag = legacyParent.getChildTextTrimOrNull(AUX_DIR)?.toBooleanStrictOrNullCompat()

        resolveLegacyPath(
            explicitPath = outputPathText,
            legacyToggle = outDirFlag,
            truePlaceholder = "${LatexPathResolver.PROJECT_DIR_PLACEHOLDER}/out",
            falsePlaceholder = LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER,
            fallback = LatexPathResolver.defaultOutputPath,
            fixInvalidBinPath = true,
        )?.let { runConfig.outputPath = it }

        resolveLegacyPath(
            explicitPath = auxPathText,
            legacyToggle = auxDirFlag,
            truePlaceholder = LatexPathResolver.defaultAuxilPath.toString(),
            falsePlaceholder = LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER,
            fallback = LatexPathResolver.defaultAuxilPath,
            fixInvalidBinPath = false,
        )?.let { runConfig.auxilPath = it }

        val workingDirectory = legacyParent.getChildTextTrimOrNull(WORKING_DIRECTORY)
        runConfig.workingDirectory = when {
            workingDirectory.isNullOrBlank() -> null
            workingDirectory == LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER -> null
            else -> pathOrNull(workingDirectory)
        }
    }

    private fun resolveLegacyPath(
        explicitPath: String?,
        legacyToggle: Boolean?,
        truePlaceholder: String,
        falsePlaceholder: String,
        fallback: Path,
        fixInvalidBinPath: Boolean,
    ): Path? {
        val explicit = explicitPath?.takeIf { it.isNotBlank() }
        if (explicit != null) {
            if (fixInvalidBinPath && isInvalidJetBrainsBinPath(explicit)) {
                return fallback
            }
            return pathOrNull(explicit) ?: fallback
        }

        val mapped = when (legacyToggle) {
            true -> truePlaceholder
            false -> falsePlaceholder
            null -> return null
        }
        return pathOrNull(mapped)
    }

    private fun buildStepBundle(
        legacyParent: Element,
    ): StepBundle {
        val mainStep = buildMainStep(legacyParent)
        val compileTwice = legacyParent.getChildTextTrimOrNull(COMPILE_TWICE)?.toBooleanStrictOrNullCompat() ?: false

        val bibResult = resolveBibtexSteps(legacyParent.getChildTextTrimOrNull(BIB_RUN_CONFIG))
        val makeindexResult = resolveMakeindexSteps(legacyParent.getChildTextTrimOrNull(MAKEINDEX_RUN_CONFIG))
        val viewerStep = buildViewerStep(legacyParent)

        val auxiliarySteps = mutableListOf<LatexStepRunConfigurationOptions>().apply {
            addAll(bibResult.steps)
            addAll(makeindexResult.steps)
        }

        val steps = mutableListOf<LatexStepRunConfigurationOptions>()
        steps += mainStep
        if (compileTwice && mainStep is LatexCompileStepOptions && auxiliarySteps.isEmpty()) {
            steps += mainStep.deepCopy().also { it.id = generateLatexStepId() }
        }
        steps += auxiliarySteps
        steps += viewerStep

        return StepBundle(steps)
    }

    private fun buildMainStep(legacyParent: Element): LatexStepRunConfigurationOptions {
        val compilerText = legacyParent.getChildTextTrimOrNull(COMPILER)
        val compilerPath = legacyParent.getChildTextTrimOrNull(COMPILER_PATH)
        val compilerArguments = legacyParent.getChildTextTrimOrNull(COMPILER_ARGUMENTS)
        val beforeRunCommand = legacyParent.getChildTextTrimOrNull(BEFORE_RUN_COMMAND)
        val outputFormatRaw = legacyParent.getChildTextTrimOrNull(OUTPUT_FORMAT)
        val outputFormat = parseOutputFormat(outputFormatRaw)

        if (compilerText.equals(LEGACY_LATEXMK, ignoreCase = true)) {
            return LatexmkCompileStepOptions().apply {
                this.compilerPath = compilerPath
                this.latexmkExtraArguments = compilerArguments ?: LatexRunConfiguration.DEFAULT_LATEXMK_EXTRA_ARGUMENTS
                this.beforeRunCommand = beforeRunCommand
                this.latexmkCompileMode = legacyOutputFormatToLatexmkMode(outputFormatRaw)
            }
        }

        val mappedClassicCompiler = LatexCompiler.entries.firstOrNull {
            it.name.equals(compilerText, ignoreCase = true)
        }
        if (mappedClassicCompiler != null) {
            return LatexCompileStepOptions().apply {
                this.compiler = mappedClassicCompiler
                this.compilerPath = compilerPath
                this.compilerArguments = compilerArguments
                this.beforeRunCommand = beforeRunCommand
                this.outputFormat = when (outputFormat) {
                    null,
                    LatexCompiler.Format.DEFAULT -> LatexCompiler.Format.PDF
                    else -> outputFormat
                }
            }
        }

        return LatexmkCompileStepOptions().apply {
            this.compilerPath = compilerPath
            this.latexmkExtraArguments = compilerArguments ?: LatexRunConfiguration.DEFAULT_LATEXMK_EXTRA_ARGUMENTS
            this.beforeRunCommand = beforeRunCommand
        }
    }

    private fun legacyOutputFormatToLatexmkMode(formatRaw: String?): LatexmkCompileMode = when (formatRaw?.trim()?.uppercase(Locale.getDefault())) {
        "DVI" -> LatexmkCompileMode.LATEX_DVI
        "PS" -> LatexmkCompileMode.LATEX_PS
        "XDV" -> LatexmkCompileMode.XELATEX_XDV
        else -> LatexmkCompileMode.PDFLATEX_PDF
    }

    private fun buildViewerStep(legacyParent: Element): PdfViewerStepOptions {
        val viewerStep = PdfViewerStepOptions()
        val viewerName = legacyParent.getChildTextTrimOrNull(PDF_VIEWER)
        if (!viewerName.isNullOrBlank()) {
            val matchedViewer = PdfViewer.availableViewers.firstOrNull {
                it.name?.equals(viewerName, ignoreCase = true) == true ||
                    it.name?.uppercase(Locale.getDefault()) == viewerName
            }
            if (matchedViewer != null) {
                viewerStep.pdfViewerName = matchedViewer.name
            }
        }

        viewerStep.requireFocus = legacyParent.getChildTextTrimOrNull(REQUIRE_FOCUS)?.toBooleanStrictOrNullCompat() ?: true
        viewerStep.customViewerCommand = legacyParent.getChildTextTrimOrNull(VIEWER_COMMAND)
        return viewerStep
    }

    private fun resolveBibtexSteps(
        legacyIdsRaw: String?,
    ): ResolvedAuxSteps<BibtexStepOptions> {
        val ids = parseLegacyIdList(legacyIdsRaw)
        if (ids.isEmpty()) {
            return ResolvedAuxSteps(emptyList(), hasDangling = false)
        }

        val steps = ids.map { id ->
            BibtexStepOptions().apply {
                legacyRunConfigId = id
            }
        }
        return ResolvedAuxSteps(steps, hasDangling = false)
    }

    private fun resolveMakeindexSteps(
        legacyIdsRaw: String?,
    ): ResolvedAuxSteps<MakeindexStepOptions> {
        val ids = parseLegacyIdList(legacyIdsRaw)
        if (ids.isEmpty()) {
            return ResolvedAuxSteps(emptyList(), hasDangling = false)
        }

        val steps = ids.map { id ->
            MakeindexStepOptions().apply {
                legacyRunConfigId = id
            }
        }
        return ResolvedAuxSteps(steps, hasDangling = false)
    }

    private fun parseLegacyIdList(raw: String?): List<String> {
        val value = raw?.trim()?.takeIf { it.isNotBlank() } ?: return emptyList()
        val unwrapped = if (value.startsWith("[") && value.endsWith("]")) {
            value.substring(1, value.length - 1)
        }
        else {
            value
        }
        return unwrapped.split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun parseDistribution(raw: String?): LatexDistributionType? {
        val value = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return LatexDistributionType.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }

    private fun parseOutputFormat(raw: String?): LatexCompiler.Format? {
        val value = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return LatexCompiler.Format.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }

    private fun Element.getChildTextTrimOrNull(name: String): String? = getChildText(name)?.trim()?.ifBlank { null }

    private fun String.toBooleanStrictOrNullCompat(): Boolean? = when (lowercase(Locale.getDefault())) {
        "true" -> true
        "false" -> false
        else -> null
    }

    /**
     * Temporary holder for base steps assembled from legacy fields before auto-completion.
     */
    private data class StepBundle(
        val baseSteps: List<LatexStepRunConfigurationOptions>,
    )

    /**
     * Represents converted auxiliary steps and whether legacy references could not be resolved.
     */
    private data class ResolvedAuxSteps<T : LatexStepRunConfigurationOptions>(
        val steps: List<T>,
        val hasDangling: Boolean,
    )
}
