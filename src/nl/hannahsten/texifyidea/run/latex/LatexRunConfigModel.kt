package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.configuration.EnvironmentVariablesData
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import java.nio.file.Path
import java.util.UUID

internal object LatexStepType {

    const val LATEX_COMPILE = "latex-compile"
    const val LATEXMK_COMPILE = "latexmk-compile"
    const val PDF_VIEWER = "pdf-viewer"
    const val BIBTEX = "bibtex"
    const val MAKEINDEX = "makeindex"
    const val EXTERNAL_TOOL = "external-tool"
    const val PYTHONTEX = "pythontex"
    const val MAKEGLOSSARIES = "makeglossaries"
    const val XINDY = "xindy"
}

internal data class LatexRunConfigModel(
    var common: LatexCommonSettings = LatexCommonSettings(),
    var steps: MutableList<LatexStepConfig> = mutableListOf(
        LatexCompileStepConfig(),
        PdfViewerStepConfig()
    ),
    var ui: LatexUiState = LatexUiState(),
) {

    fun deepCopy(): LatexRunConfigModel = LatexRunConfigModel(
        common = common.copy(
            environmentVariables = common.environmentVariables,
        ),
        steps = steps.map { it.deepCopy() }.toMutableList(),
        ui = LatexUiState(
            stepUiOptionIdsByStepId = ui.stepUiOptionIdsByStepId
                .mapValues { (_, ids) -> ids.toMutableSet() }
                .toMutableMap()
        ),
    )
}

internal data class LatexCommonSettings(
    var mainFilePath: String? = null,
    var workingDirectory: Path? = null,
    var outputPath: Path? = LatexPathResolver.defaultOutputPath,
    var auxilPath: Path? = LatexPathResolver.defaultAuxilPath,
    var environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT,
    var expandMacrosEnvVariables: Boolean = false,
    var latexDistribution: LatexDistributionType = LatexDistributionType.MODULE_SDK,
)

internal data class LatexUiState(
    var stepUiOptionIdsByStepId: MutableMap<String, MutableSet<String>> = mutableMapOf(),
)

internal sealed interface LatexStepConfig {

    val id: String
    val type: String
    var enabled: Boolean

    fun deepCopy(): LatexStepConfig
}

internal data class LatexCompileStepConfig(
    override val id: String = generateLatexStepId(),
    override var enabled: Boolean = true,
    var compiler: LatexCompiler = LatexCompiler.PDFLATEX,
    var compilerPath: String? = null,
    var compilerArguments: String? = null,
    var outputFormat: Format = Format.PDF,
    var beforeRunCommand: String? = null,
) : LatexStepConfig {

    override val type: String = LatexStepType.LATEX_COMPILE

    override fun deepCopy(): LatexStepConfig = copy()
}

internal data class LatexmkCompileStepConfig(
    override val id: String = generateLatexStepId(),
    override var enabled: Boolean = true,
    var compilerPath: String? = null,
    var compilerArguments: String? = null,
    var latexmkCompileMode: LatexmkCompileMode = LatexmkCompileMode.AUTO,
    var latexmkCustomEngineCommand: String? = null,
    var latexmkCitationTool: LatexmkCitationTool = LatexmkCitationTool.AUTO,
    var latexmkExtraArguments: String? = LatexRunConfiguration.DEFAULT_LATEXMK_EXTRA_ARGUMENTS,
    var beforeRunCommand: String? = null,
) : LatexStepConfig {

    override val type: String = LatexStepType.LATEXMK_COMPILE

    override fun deepCopy(): LatexStepConfig = copy()
}

internal data class PdfViewerStepConfig(
    override val id: String = generateLatexStepId(),
    override var enabled: Boolean = true,
    var pdfViewerName: String? = PdfViewer.firstAvailableViewer?.name,
    var requireFocus: Boolean = true,
    var customViewerCommand: String? = null,
) : LatexStepConfig {

    override val type: String = LatexStepType.PDF_VIEWER

    override fun deepCopy(): LatexStepConfig = copy()
}

internal data class BibtexStepConfig(
    override val id: String = generateLatexStepId(),
    override var enabled: Boolean = true,
    var bibliographyCompiler: BibliographyCompiler = BibliographyCompiler.BIBTEX,
    var compilerPath: String? = null,
    var compilerArguments: String? = null,
) : LatexStepConfig {

    override val type: String = LatexStepType.BIBTEX

    override fun deepCopy(): LatexStepConfig = copy()
}

internal data class MakeindexStepConfig(
    override val id: String = generateLatexStepId(),
    override var enabled: Boolean = true,
    var program: MakeindexProgram = MakeindexProgram.MAKEINDEX,
    var commandLineArguments: String? = null,
) : LatexStepConfig {

    override val type: String = LatexStepType.MAKEINDEX

    override fun deepCopy(): LatexStepConfig = copy()
}

internal data class ExternalToolStepConfig(
    override val id: String = generateLatexStepId(),
    override var enabled: Boolean = true,
    var commandLine: String? = null,
) : LatexStepConfig {

    override val type: String = LatexStepType.EXTERNAL_TOOL

    override fun deepCopy(): LatexStepConfig = copy()
}

internal data class PythontexStepConfig(
    override val id: String = generateLatexStepId(),
    override var enabled: Boolean = true,
    var commandLine: String? = null,
) : LatexStepConfig {

    override val type: String = LatexStepType.PYTHONTEX

    override fun deepCopy(): LatexStepConfig = copy()
}

internal data class MakeglossariesStepConfig(
    override val id: String = generateLatexStepId(),
    override var enabled: Boolean = true,
    var commandLine: String? = null,
) : LatexStepConfig {

    override val type: String = LatexStepType.MAKEGLOSSARIES

    override fun deepCopy(): LatexStepConfig = copy()
}

internal data class XindyStepConfig(
    override val id: String = generateLatexStepId(),
    override var enabled: Boolean = true,
    var commandLine: String? = null,
) : LatexStepConfig {

    override val type: String = LatexStepType.XINDY

    override fun deepCopy(): LatexStepConfig = copy()
}

internal fun generateLatexStepId(): String = UUID.randomUUID().toString()

internal fun defaultStepFor(type: String): LatexStepConfig? = when (type.trim().lowercase()) {
    LatexStepType.LATEX_COMPILE -> LatexCompileStepConfig()
    LatexStepType.LATEXMK_COMPILE -> LatexmkCompileStepConfig()
    LatexStepType.PDF_VIEWER -> PdfViewerStepConfig()
    LatexStepType.BIBTEX -> BibtexStepConfig()
    LatexStepType.MAKEINDEX -> MakeindexStepConfig()
    LatexStepType.EXTERNAL_TOOL -> ExternalToolStepConfig()
    LatexStepType.PYTHONTEX -> PythontexStepConfig()
    LatexStepType.MAKEGLOSSARIES -> MakeglossariesStepConfig()
    LatexStepType.XINDY -> XindyStepConfig()
    else -> null
}
