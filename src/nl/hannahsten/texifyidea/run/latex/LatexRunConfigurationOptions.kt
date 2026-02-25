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

class LatexRunConfigurationOptions : LocatableRunConfigurationOptions() {

    var mainFilePath by string(null)
    var workingDirectoryPath by string(null)
    var outputPath by string(LatexPathResolver.defaultOutputPath.toString())
    var auxilPath by string(LatexPathResolver.defaultAuxilPath.toString())

    var latexDistribution by enum(LatexDistributionType.MODULE_SDK)
    var expandMacrosEnvVariables by property(false)
    var passParentEnvironmentVariables by property(true)

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
        if (steps.none { it.enabled }) {
            steps = defaultLatexmkSteps()
        }
    }
}

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

    @get:Attribute("enabled")
    var enabled by property(true)

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

@Tag("pdfViewer")
class PdfViewerStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.PDF_VIEWER

    var pdfViewerName by string(PdfViewer.firstAvailableViewer.name)
    var requireFocus by property(true)
    var customViewerCommand by string(null)

    override fun newInstance(): LatexStepRunConfigurationOptions = PdfViewerStepOptions()
}

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

@Tag("externalTool")
class ExternalToolStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.EXTERNAL_TOOL

    var executable by string(null)
    var arguments by string(null)
    var workingDirectoryPath by string(null)
    var beforeRunCommand by string(null)

    override fun newInstance(): LatexStepRunConfigurationOptions = ExternalToolStepOptions()
}

@Tag("pythontex")
class PythontexStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.PYTHONTEX

    var executable by string("pythontex")
    var arguments by string(null)
    var workingDirectoryPath by string(null)
    var beforeRunCommand by string(null)

    override fun newInstance(): LatexStepRunConfigurationOptions = PythontexStepOptions()
}

@Tag("makeglossaries")
class MakeglossariesStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.MAKEGLOSSARIES

    var executable by string("makeglossaries")
    var arguments by string(null)
    var workingDirectoryPath by string(null)
    var beforeRunCommand by string(null)

    override fun newInstance(): LatexStepRunConfigurationOptions = MakeglossariesStepOptions()
}

@Tag("xindy")
class XindyStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.XINDY

    var executable by string("xindy")
    var arguments by string(null)
    var workingDirectoryPath by string(null)
    var beforeRunCommand by string(null)

    override fun newInstance(): LatexStepRunConfigurationOptions = XindyStepOptions()
}

@Tag("fileCleanup")
class FileCleanupStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.FILE_CLEANUP

    override fun newInstance(): LatexStepRunConfigurationOptions = FileCleanupStepOptions()
}

internal fun generateLatexStepId(): String = UUID.randomUUID().toString()

internal fun defaultClassicSteps(): MutableList<LatexStepRunConfigurationOptions> = mutableListOf(
    LatexCompileStepOptions(),
    PdfViewerStepOptions(),
)

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
