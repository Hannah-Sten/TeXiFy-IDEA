package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.configurations.LocatableRunConfigurationOptions
import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.execution.ui.FragmentedSettings
import com.intellij.openapi.components.BaseState
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import java.util.UUID

/**
 * Canonical identifiers for supported LaTeX step kinds in run configuration storage.
 * These ids are shared across persistence, UI selection, and runtime step-provider lookup.
 */
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
    const val FILE_CLEANUP = "file-cleanup"
}

/**
 * Serializable options container for [LatexRunConfiguration].
 * It stores common run settings and the ordered step schema consumed by execution planning.
 */
class LatexRunConfigurationOptions : LocatableRunConfigurationOptions() {

    var mainFilePath by string(null)
    var workingDirectoryPath by string(null)
    var outputPath by string(LatexPathResolver.defaultOutputPath.toString())
    var auxilPath by string(LatexPathResolver.defaultAuxilPath.toString())

    var latexDistribution by enum(LatexDistributionType.MODULE_SDK)
    var expandMacrosEnvVariables by property(false)
    var passParentEnvironmentVariables by property(true)

    /**
     * Serializable key/value pair for a single environment variable.
     * Entries are used to build [com.intellij.execution.configuration.EnvironmentVariablesData] at runtime.
     */
    @Tag("entry")
    class EnvironmentVariableEntry : BaseState() {

        @get:Attribute("name")
        var name by string(null)

        @get:Attribute("value")
        var value by string(null)

        fun deepCopy(): EnvironmentVariableEntry = EnvironmentVariableEntry().also { it.copyFrom(this) }
    }

    @get:XCollection(propertyElementName = "environmentVariables")
    var environmentVariables: MutableList<EnvironmentVariableEntry> by list()

    @get:XCollection(
        propertyElementName = "steps",
        elementName = "step",
        elementTypes = [
            LatexCompileStepOptions::class,
            LatexmkCompileStepOptions::class,
            PdfViewerStepOptions::class,
            BibtexStepOptions::class,
            MakeindexStepOptions::class,
            ExternalToolStepOptions::class,
            PythontexStepOptions::class,
            MakeglossariesStepOptions::class,
            XindyStepOptions::class,
            FileCleanupStepOptions::class,
        ]
    )
    var steps: MutableList<LatexStepRunConfigurationOptions> by list()

    fun ensureDefaultSteps() {
        if (steps.isEmpty()) {
            steps = defaultLatexmkSteps()
        }
    }
}

/**
 * Base persisted model for one compile-sequence step entry.
 * Subclasses define step-specific fields, while this type provides shared identity and deep-copy behavior.
 */
abstract class LatexStepRunConfigurationOptions : RunConfigurationOptions() {

    @get:Attribute("id")
    var persistedId by string(generateLatexStepId())

    var id: String
        get() {
            if (persistedId.isNullOrBlank()) {
                persistedId = generateLatexStepId()
            }
            return persistedId!!
        }
        set(value) {
            persistedId = value
        }

    @get:Attribute("type")
    abstract var type: String

    fun deepCopy(): LatexStepRunConfigurationOptions {
        val copied = newInstance()
        copied.copyFrom(this)
        copied.selectedOptions.clear()
        copied.selectedOptions.addAll(
            selectedOptions
                .map { source ->
                    FragmentedSettings.Option().also { option ->
                        option.copyFrom(source)
                    }
                }
                .toMutableList()
        )
        return copied
    }

    protected abstract fun newInstance(): LatexStepRunConfigurationOptions
}

/**
 * Persisted options for a classic LaTeX compiler invocation step.
 * The runtime maps this model to a process step using compiler, arguments, and output format fields.
 */
@Tag("latexCompile")
class LatexCompileStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.LATEX_COMPILE

    var compiler by enum(LatexCompiler.PDFLATEX)
    var compilerPath by string(null)
    var compilerArguments by string(null)
    var outputFormat by enum(LatexCompiler.Format.PDF)
    var beforeRunCommand by string(null)

    override fun newInstance(): LatexStepRunConfigurationOptions = LatexCompileStepOptions()
}

/**
 * Persisted options for a `latexmk`-based compile step.
 * Execution uses this model to resolve effective latexmk mode, citation tool, and extra arguments.
 */
@Tag("latexmkCompile")
class LatexmkCompileStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.LATEXMK_COMPILE

    var compilerPath by string(null)
    var compilerArguments by string(null)
    var latexmkCompileMode by enum(LatexmkCompileMode.AUTO)
    var latexmkCustomEngineCommand by string(null)
    var latexmkCitationTool by enum(LatexmkCitationTool.AUTO)
    var latexmkExtraArguments by string(LatexRunConfiguration.DEFAULT_LATEXMK_EXTRA_ARGUMENTS)
    var beforeRunCommand by string(null)

    override fun newInstance(): LatexStepRunConfigurationOptions = LatexmkCompileStepOptions()
}

/**
 * Persisted options for opening the generated PDF after compilation.
 * This step configures viewer command and focus behavior but does not compile sources.
 */
@Tag("pdfViewer")
class PdfViewerStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.PDF_VIEWER

    var pdfViewerName by string(PdfViewer.firstAvailableViewer.name)
    var requireFocus by property(true)
    var customViewerCommand by string(null)

    override fun newInstance(): LatexStepRunConfigurationOptions = PdfViewerStepOptions()
}

/**
 * Persisted options for bibliography tool execution.
 * The run pipeline consumes this model as an auxiliary step between compile steps when needed.
 */
@Tag("bibtex")
class BibtexStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.BIBTEX

    var bibliographyCompiler by enum(BibliographyCompiler.BIBTEX)
    var compilerPath by string(null)
    var compilerArguments by string(null)
    var workingDirectoryPath by string(null)
    var beforeRunCommand by string(null)

    override fun newInstance(): LatexStepRunConfigurationOptions = BibtexStepOptions()
}

/**
 * Persisted options for index generation steps.
 * It carries selected makeindex-like program settings used by the corresponding runtime step.
 */
@Tag("makeindex")
class MakeindexStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.MAKEINDEX

    var program by enum(MakeindexProgram.MAKEINDEX)
    var commandLineArguments by string(null)
    var workingDirectoryPath by string(null)
    var targetBaseNameOverride by string(null)
    var beforeRunCommand by string(null)

    override fun newInstance(): LatexStepRunConfigurationOptions = MakeindexStepOptions()
}

/**
 * Persisted options for running an arbitrary external command in the sequence.
 * This model defines executable, arguments, and working directory for the command step.
 */
@Tag("externalTool")
class ExternalToolStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.EXTERNAL_TOOL

    var executable by string(null)
    var arguments by string(null)
    var workingDirectoryPath by string(null)
    var beforeRunCommand by string(null)

    override fun newInstance(): LatexStepRunConfigurationOptions = ExternalToolStepOptions()
}

/**
 * Persisted options for invoking PythonTeX commands.
 * The command is executed as a generic command-line follow-up step in the sequence.
 */
@Tag("pythontex")
class PythontexStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.PYTHONTEX

    var executable by string("pythontex")
    var arguments by string(null)
    var workingDirectoryPath by string(null)
    var beforeRunCommand by string(null)

    override fun newInstance(): LatexStepRunConfigurationOptions = PythontexStepOptions()
}

/**
 * Persisted options for running makeglossaries-style commands.
 * It represents glossary generation as a reusable command step in the run pipeline.
 */
@Tag("makeglossaries")
class MakeglossariesStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.MAKEGLOSSARIES

    var executable by string("makeglossaries")
    var arguments by string(null)
    var workingDirectoryPath by string(null)
    var beforeRunCommand by string(null)

    override fun newInstance(): LatexStepRunConfigurationOptions = MakeglossariesStepOptions()
}

/**
 * Persisted options for running xindy-style indexing commands.
 * This step model is consumed by the generic command execution layer.
 */
@Tag("xindy")
class XindyStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.XINDY

    var executable by string("xindy")
    var arguments by string(null)
    var workingDirectoryPath by string(null)
    var beforeRunCommand by string(null)

    override fun newInstance(): LatexStepRunConfigurationOptions = XindyStepOptions()
}

/**
 * Marker options for post-run generated-file cleanup.
 * The runtime cleanup step uses this type presence as configuration and has no extra fields.
 */
@Tag("fileCleanup")
class FileCleanupStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.FILE_CLEANUP

    override fun newInstance(): LatexStepRunConfigurationOptions = FileCleanupStepOptions()
}

internal fun generateLatexStepId(): String = UUID.randomUUID().toString()

internal fun defaultLatexmkSteps(): MutableList<LatexStepRunConfigurationOptions> = mutableListOf(
    LatexmkCompileStepOptions(),
    PdfViewerStepOptions(),
)

internal fun defaultStepFor(type: String): LatexStepRunConfigurationOptions? = when (type.trim().lowercase()) {
    LatexStepType.LATEX_COMPILE -> LatexCompileStepOptions()
    LatexStepType.LATEXMK_COMPILE -> LatexmkCompileStepOptions()
    LatexStepType.PDF_VIEWER -> PdfViewerStepOptions()
    LatexStepType.BIBTEX -> BibtexStepOptions()
    LatexStepType.MAKEINDEX -> MakeindexStepOptions()
    LatexStepType.EXTERNAL_TOOL -> ExternalToolStepOptions()
    LatexStepType.PYTHONTEX -> PythontexStepOptions()
    LatexStepType.MAKEGLOSSARIES -> MakeglossariesStepOptions()
    LatexStepType.XINDY -> XindyStepOptions()
    LatexStepType.FILE_CLEANUP -> FileCleanupStepOptions()
    else -> null
}
