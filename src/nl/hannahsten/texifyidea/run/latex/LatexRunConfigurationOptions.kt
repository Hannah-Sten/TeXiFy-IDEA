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

        fun deepCopy(): EnvironmentVariableEntry {
            val copied = EnvironmentVariableEntry()
            copied.name = name
            copied.value = value
            return copied
        }
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
        ]
    )
    var steps: MutableList<LatexStepRunConfigurationOptions> by list()

    fun ensureDefaultSteps() {
        if (steps.none { it.enabled }) {
            steps = mutableListOf(LatexCompileStepOptions(), PdfViewerStepOptions())
        }
    }

    fun deepCopy(): LatexRunConfigurationOptions {
        val copied = LatexRunConfigurationOptions()
        copied.mainFilePath = mainFilePath
        copied.workingDirectoryPath = workingDirectoryPath
        copied.outputPath = outputPath
        copied.auxilPath = auxilPath
        copied.latexDistribution = latexDistribution
        copied.expandMacrosEnvVariables = expandMacrosEnvVariables
        copied.passParentEnvironmentVariables = passParentEnvironmentVariables
        copied.environmentVariables = environmentVariables.map { it.deepCopy() }.toMutableList()
        copied.steps = steps.map { it.deepCopy() }.toMutableList()
        return copied
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

    abstract fun deepCopy(): LatexStepRunConfigurationOptions

    protected fun copySelectedOptionsFrom(other: LatexStepRunConfigurationOptions) {
        selectedOptions = other.selectedOptions
            .map { FragmentedSettings.Option(it.name ?: "", it.visible) }
            .toMutableList()
    }
}

@Tag("latexCompile")
class LatexCompileStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.LATEX_COMPILE

    var compiler by enum(LatexCompiler.PDFLATEX)
    var compilerPath by string(null)
    var compilerArguments by string(null)
    var outputFormat by enum(LatexCompiler.Format.PDF)
    var beforeRunCommand by string(null)

    override fun deepCopy(): LatexStepRunConfigurationOptions {
        val copied = LatexCompileStepOptions()
        copied.id = id
        copied.enabled = enabled
        copied.type = type
        copied.compiler = compiler
        copied.compilerPath = compilerPath
        copied.compilerArguments = compilerArguments
        copied.outputFormat = outputFormat
        copied.beforeRunCommand = beforeRunCommand
        copied.copySelectedOptionsFrom(this)
        return copied
    }
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

    override fun deepCopy(): LatexStepRunConfigurationOptions {
        val copied = LatexmkCompileStepOptions()
        copied.id = id
        copied.enabled = enabled
        copied.type = type
        copied.compilerPath = compilerPath
        copied.compilerArguments = compilerArguments
        copied.latexmkCompileMode = latexmkCompileMode
        copied.latexmkCustomEngineCommand = latexmkCustomEngineCommand
        copied.latexmkCitationTool = latexmkCitationTool
        copied.latexmkExtraArguments = latexmkExtraArguments
        copied.beforeRunCommand = beforeRunCommand
        copied.copySelectedOptionsFrom(this)
        return copied
    }
}

@Tag("pdfViewer")
class PdfViewerStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.PDF_VIEWER

    var pdfViewerName by string(PdfViewer.firstAvailableViewer?.name)
    var requireFocus by property(true)
    var customViewerCommand by string(null)

    override fun deepCopy(): LatexStepRunConfigurationOptions {
        val copied = PdfViewerStepOptions()
        copied.id = id
        copied.enabled = enabled
        copied.type = type
        copied.pdfViewerName = pdfViewerName
        copied.requireFocus = requireFocus
        copied.customViewerCommand = customViewerCommand
        copied.copySelectedOptionsFrom(this)
        return copied
    }
}

@Tag("bibtex")
class BibtexStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.BIBTEX

    var bibliographyCompiler by enum(BibliographyCompiler.BIBTEX)
    var compilerPath by string(null)
    var compilerArguments by string(null)

    override fun deepCopy(): LatexStepRunConfigurationOptions {
        val copied = BibtexStepOptions()
        copied.id = id
        copied.enabled = enabled
        copied.type = type
        copied.bibliographyCompiler = bibliographyCompiler
        copied.compilerPath = compilerPath
        copied.compilerArguments = compilerArguments
        copied.copySelectedOptionsFrom(this)
        return copied
    }
}

@Tag("makeindex")
class MakeindexStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.MAKEINDEX

    var program by enum(MakeindexProgram.MAKEINDEX)
    var commandLineArguments by string(null)

    override fun deepCopy(): LatexStepRunConfigurationOptions {
        val copied = MakeindexStepOptions()
        copied.id = id
        copied.enabled = enabled
        copied.type = type
        copied.program = program
        copied.commandLineArguments = commandLineArguments
        copied.copySelectedOptionsFrom(this)
        return copied
    }
}

@Tag("externalTool")
class ExternalToolStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.EXTERNAL_TOOL

    var commandLine by string(null)

    override fun deepCopy(): LatexStepRunConfigurationOptions {
        val copied = ExternalToolStepOptions()
        copied.id = id
        copied.enabled = enabled
        copied.type = type
        copied.commandLine = commandLine
        copied.copySelectedOptionsFrom(this)
        return copied
    }
}

@Tag("pythontex")
class PythontexStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.PYTHONTEX

    var commandLine by string(null)

    override fun deepCopy(): LatexStepRunConfigurationOptions {
        val copied = PythontexStepOptions()
        copied.id = id
        copied.enabled = enabled
        copied.type = type
        copied.commandLine = commandLine
        copied.copySelectedOptionsFrom(this)
        return copied
    }
}

@Tag("makeglossaries")
class MakeglossariesStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.MAKEGLOSSARIES

    var commandLine by string(null)

    override fun deepCopy(): LatexStepRunConfigurationOptions {
        val copied = MakeglossariesStepOptions()
        copied.id = id
        copied.enabled = enabled
        copied.type = type
        copied.commandLine = commandLine
        copied.copySelectedOptionsFrom(this)
        return copied
    }
}

@Tag("xindy")
class XindyStepOptions : LatexStepRunConfigurationOptions() {

    override var type: String = LatexStepType.XINDY

    var commandLine by string(null)

    override fun deepCopy(): LatexStepRunConfigurationOptions {
        val copied = XindyStepOptions()
        copied.id = id
        copied.enabled = enabled
        copied.type = type
        copied.commandLine = commandLine
        copied.copySelectedOptionsFrom(this)
        return copied
    }
}

internal fun generateLatexStepId(): String = UUID.randomUUID().toString()

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
    else -> null
}
