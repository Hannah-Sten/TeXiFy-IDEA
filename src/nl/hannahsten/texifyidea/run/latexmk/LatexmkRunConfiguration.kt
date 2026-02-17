package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.WriteExternalException
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.util.LatexmkRcFileFinder
import org.jdom.Element

class LatexmkRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : LatexRunConfiguration(project, factory, name) {

    companion object {
        private const val PARENT = "texify-latexmk"
        private const val ENGINE_MODE = "engine-mode"
        private const val CUSTOM_ENGINE_COMMAND = "custom-engine-command"
        private const val LATEXMK_OUTPUT_FORMAT = "latexmk-output-format"
        private const val CITATION_TOOL = "citation-tool"
        private const val EXTRA_ARGUMENTS = "extra-arguments"
    }

    var engineMode: LatexmkEngineMode = LatexmkEngineMode.PDFLATEX

    var customEngineCommand: String? = null
        set(value) {
            field = value?.trim()?.ifEmpty { null }
        }

    var latexmkOutputFormat: LatexmkOutputFormat = LatexmkOutputFormat.DEFAULT

    var citationTool: LatexmkCitationTool = LatexmkCitationTool.AUTO

    var extraArguments: String? = null
        set(value) {
            field = value?.trim()?.ifEmpty { null }
        }

    init {
        compiler = LatexCompiler.LATEXMK
        outputFormat = Format.DEFAULT
        compileTwice = false
    }

    override fun getConfigurationEditor() = LatexmkSettingsEditor(project)

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        compiler = LatexCompiler.LATEXMK
        outputFormat = Format.DEFAULT
        compileTwice = false
        bibRunConfigs = emptySet()
        makeindexRunConfigs = emptySet()
        compilerArguments = buildLatexmkArguments()
        return LatexmkCommandLineState(environment, this)
    }

    override fun getOutputFilePath(): String {
        val outputDir = outputPath.getAndCreatePath() ?: mainFile?.parent
        val ext = latexmkOutputFormat.extension
        return "${outputDir?.path}/${mainFile?.nameWithoutExtension ?: "main"}.$ext"
    }

    fun buildLatexmkArguments(): String {
        val arguments = mutableListOf<String>()
        val hasRcFile = LatexmkRcFileFinder.isLatexmkRcFilePresent(this)

        val hasExplicitStructuredOptions =
            engineMode != LatexmkEngineMode.PDFLATEX ||
                latexmkOutputFormat != LatexmkOutputFormat.DEFAULT ||
                citationTool != LatexmkCitationTool.AUTO ||
                !customEngineCommand.isNullOrBlank()

        if (!hasRcFile || hasExplicitStructuredOptions) {
            arguments += engineMode.toLatexmkFlags(customEngineCommand)
            arguments += latexmkOutputFormat.toLatexmkFlags()
            arguments += citationTool.toLatexmkFlags()
        }

        extraArguments?.let { arguments += ParametersListUtil.parse(it) }

        return ParametersListUtil.join(arguments)
    }

    @Throws(InvalidDataException::class)
    override fun readExternal(element: Element) {
        super.readExternal(element)

        val parent = element.getChild(PARENT) ?: return

        engineMode = parent.getChildText(ENGINE_MODE)?.let {
            runCatching { LatexmkEngineMode.valueOf(it) }.getOrDefault(LatexmkEngineMode.PDFLATEX)
        } ?: LatexmkEngineMode.PDFLATEX

        customEngineCommand = parent.getChildText(CUSTOM_ENGINE_COMMAND)

        latexmkOutputFormat = parent.getChildText(LATEXMK_OUTPUT_FORMAT)?.let {
            runCatching { LatexmkOutputFormat.valueOf(it) }.getOrDefault(LatexmkOutputFormat.DEFAULT)
        } ?: LatexmkOutputFormat.DEFAULT

        citationTool = parent.getChildText(CITATION_TOOL)?.let {
            runCatching { LatexmkCitationTool.valueOf(it) }.getOrDefault(LatexmkCitationTool.AUTO)
        } ?: LatexmkCitationTool.AUTO

        extraArguments = parent.getChildText(EXTRA_ARGUMENTS)

        compiler = LatexCompiler.LATEXMK
        outputFormat = Format.DEFAULT
        compileTwice = false
    }

    @Throws(WriteExternalException::class)
    override fun writeExternal(element: Element) {
        super.writeExternal(element)

        val parent = element.getChild(PARENT) ?: Element(PARENT).also { element.addContent(it) }
        parent.removeContent()

        parent.addContent(Element(ENGINE_MODE).also { it.text = engineMode.name })
        parent.addContent(Element(CUSTOM_ENGINE_COMMAND).also { it.text = customEngineCommand ?: "" })
        parent.addContent(Element(LATEXMK_OUTPUT_FORMAT).also { it.text = latexmkOutputFormat.name })
        parent.addContent(Element(CITATION_TOOL).also { it.text = citationTool.name })
        parent.addContent(Element(EXTRA_ARGUMENTS).also { it.text = extraArguments ?: "" })
    }

    override fun suggestedName(): String? = mainFile?.nameWithoutExtension?.plus(" (latexmk)")
}

private fun LatexmkEngineMode.toLatexmkFlags(customEngineCommand: String?): List<String> = when (this) {
    LatexmkEngineMode.PDFLATEX -> listOf("-pdf")
    LatexmkEngineMode.XELATEX -> listOf("-xelatex")
    LatexmkEngineMode.LUALATEX -> listOf("-lualatex")
    LatexmkEngineMode.LATEX -> listOf("-latex")
    LatexmkEngineMode.CUSTOM_COMMAND -> customEngineCommand?.let {
        val escaped = it.replace("\"", "\\\"")
        listOf("-pdflatex=\"$escaped\"")
    } ?: emptyList()
}

private fun LatexmkOutputFormat.toLatexmkFlags(): List<String> = when (this) {
    LatexmkOutputFormat.DEFAULT -> emptyList()
    LatexmkOutputFormat.PDF -> listOf("-pdf")
    LatexmkOutputFormat.DVI -> listOf("-dvi")
    LatexmkOutputFormat.PS -> listOf("-ps")
    LatexmkOutputFormat.XDV -> listOf("-xdv")
}

private fun LatexmkCitationTool.toLatexmkFlags(): List<String> = when (this) {
    LatexmkCitationTool.AUTO -> emptyList()
    LatexmkCitationTool.BIBTEX -> listOf("-bibtex")
    LatexmkCitationTool.BIBER -> listOf("-use-biber")
    LatexmkCitationTool.DISABLED -> listOf("-bibtex-")
}
