package nl.hannahsten.texifyidea.run.compiler

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.common.DockerCommandSupport
import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.files.hasTectonicTomlFile
import nl.hannahsten.texifyidea.util.runCommand
import java.util.Locale

@Suppress("DuplicatedCode")
enum class LatexCompiler(private val displayName: String, val executableName: String) {

    PDFLATEX("pdfLaTeX", "pdflatex") {

        override fun createCommand(
            session: LatexRunSessionState,
            stepConfig: LatexCompileStepOptions,
            auxilPath: String?,
            outputPath: String,
            moduleRoots: Array<VirtualFile>,
        ): MutableList<String> {
            val executable = stepConfig.compilerPath ?: LatexSdkUtil.getExecutableName(
                executableName,
                session.project,
                session.latexSdk,
                session.distributionType,
            )
            return mutableListOf(executable).apply {
                add("-file-line-error")
                add("-interaction=nonstopmode")
                add("-synctex=1")
                add("-output-format=${stepConfig.outputFormat.name.lowercase(Locale.getDefault())}")
                add("-output-directory=$outputPath")

                if (auxilPath != null && session.distributionType.isMiktex(project = session.project)) {
                    add("-aux-directory=$auxilPath")
                }

                if (session.distributionType.isMiktex(session.project)) {
                    moduleRoots.forEach {
                        add("-include-directory=${it.path}")
                    }
                }
            }
        }
    },

    LUALATEX("LuaLaTeX", "lualatex") {

        override fun createCommand(
            session: LatexRunSessionState,
            stepConfig: LatexCompileStepOptions,
            auxilPath: String?,
            outputPath: String,
            moduleRoots: Array<VirtualFile>,
        ): MutableList<String> {
            val executable = stepConfig.compilerPath ?: LatexSdkUtil.getExecutableName(
                executableName,
                session.project,
                session.latexSdk,
                session.distributionType,
            )
            return mutableListOf(executable).apply {
                add("-file-line-error")
                add("-interaction=nonstopmode")
                add("-synctex=1")
                add("-output-format=${stepConfig.outputFormat.name.lowercase(Locale.getDefault())}")
                add("-output-directory=$outputPath")
            }
        }
    },

    XELATEX("XeLaTeX", "xelatex") {

        override val outputFormats = arrayOf(Format.PDF, Format.XDV)

        override fun createCommand(
            session: LatexRunSessionState,
            stepConfig: LatexCompileStepOptions,
            auxilPath: String?,
            outputPath: String,
            moduleRoots: Array<VirtualFile>,
        ): MutableList<String> {
            val executable = stepConfig.compilerPath ?: LatexSdkUtil.getExecutableName(
                executableName,
                session.project,
                session.latexSdk,
                session.distributionType,
            )
            return mutableListOf(executable).apply {
                add("-file-line-error")
                add("-interaction=nonstopmode")
                add("-synctex=1")

                if (stepConfig.outputFormat == Format.XDV) {
                    add("-no-pdf")
                }

                add("-output-directory=$outputPath")

                if (auxilPath != null && session.distributionType.isMiktex(session.project)) {
                    add("-aux-directory=$auxilPath")
                }

                if (session.distributionType.isMiktex(session.project)) {
                    moduleRoots.forEach {
                        add("-include-directory=${it.path}")
                    }
                }
            }
        }
    },

    TEXLIVEONFLY("Texliveonfly", "texliveonfly") {

        override val outputFormats = arrayOf(Format.PDF)

        override fun createCommand(
            session: LatexRunSessionState,
            stepConfig: LatexCompileStepOptions,
            auxilPath: String?,
            outputPath: String,
            moduleRoots: Array<VirtualFile>,
        ): MutableList<String> {
            val executable = stepConfig.compilerPath ?: LatexSdkUtil.getExecutableName(
                executableName,
                session.project,
                session.latexSdk,
                session.distributionType,
            )
            return mutableListOf(executable).apply {
                add("--arguments=--output-directory=$outputPath")
            }
        }
    },

    TECTONIC("Tectonic", "tectonic") {

        override val includesBibtex = true

        override val handlesNumberOfCompiles = true

        override val outputFormats = arrayOf(Format.PDF, Format.HTML, Format.XDV, Format.AUX)

        override fun createCommand(
            session: LatexRunSessionState,
            stepConfig: LatexCompileStepOptions,
            auxilPath: String?,
            outputPath: String,
            moduleRoots: Array<VirtualFile>,
        ): MutableList<String> {
            val executable = stepConfig.compilerPath ?: executableName
            return mutableListOf(executable).apply {
                if (!session.mainFile.hasTectonicTomlFile()) {
                    add("--synctex")
                    add("--outfmt=${stepConfig.outputFormat.name.lowercase(Locale.getDefault())}")
                    add("--outdir=$outputPath")
                }
                else {
                    add("-X")
                    add("build")
                }
            }
        }
    },

    ARARA("Arara", "arara") {

        override val includesBibtex = true

        override val handlesNumberOfCompiles = true

        override val outputFormats = arrayOf(Format.PDF)

        override fun createCommand(
            session: LatexRunSessionState,
            stepConfig: LatexCompileStepOptions,
            auxilPath: String?,
            outputPath: String,
            moduleRoots: Array<VirtualFile>,
        ): MutableList<String> = mutableListOf(stepConfig.compilerPath ?: executableName)
    },
    ;

    fun buildCommand(
        session: LatexRunSessionState,
        stepConfig: LatexCompileStepOptions,
    ): List<String> {
        val mainFile = session.mainFile
        val moduleRoots = if (session.distributionType.isDocker()) {
            emptyArray()
        }
        else {
            val allRoots = ModuleUtil.findModuleForFile(mainFile, session.project)?.rootManager?.sourceRoots ?: emptyArray()
            var totalLength = 0
            val roots = mutableListOf<VirtualFile>()
            for (root in allRoots) {
                totalLength += root.toString().length + " -include-directory=".length
                if (totalLength > 10_000) break
                roots.add(root)
            }
            roots.toTypedArray()
        }

        val outputPath = when (session.distributionType) {
            LatexDistributionType.DOCKER_MIKTEX -> "/miktex/out"
            LatexDistributionType.DOCKER_TEXLIVE -> "/out"
            else -> session.outputDir.path.toWslPathIfNeeded(session.distributionType)
        }

        val auxilPath = when (session.distributionType) {
            LatexDistributionType.DOCKER_MIKTEX -> "/miktex/auxil"
            LatexDistributionType.DOCKER_TEXLIVE -> null
            else -> session.auxDir?.path?.toWslPathIfNeeded(session.distributionType)
        }

        val command = createCommand(
            session = session,
            stepConfig = stepConfig,
            auxilPath = auxilPath,
            outputPath = outputPath,
            moduleRoots = moduleRoots,
        )

        if (session.distributionType == LatexDistributionType.WSL_TEXLIVE) {
            var wslCommand = GeneralCommandLine(command).commandLineString
            stepConfig.compilerArguments
                ?.takeIf(String::isNotBlank)
                ?.let { arguments ->
                    ParametersListUtil.parse(arguments).forEach { wslCommand += " $it" }
                }
            wslCommand += " ${mainFile.path.toWslPathIfNeeded(session.distributionType)}"
            return mutableListOf(*SystemEnvironment.wslCommand, wslCommand)
        }

        if (session.distributionType.isDocker()) {
            DockerCommandSupport.prependDockerRunCommand(
                session = session,
                dockerAuxDir = auxilPath,
                dockerOutputDir = outputPath,
                command = command,
            )
        }

        stepConfig.compilerArguments
            ?.takeIf(String::isNotBlank)
            ?.let { arguments ->
                ParametersListUtil.parse(arguments).forEach(command::add)
            }

        if (stepConfig.beforeRunCommand?.isNotBlank() == true) {
            command.add(stepConfig.beforeRunCommand + " \\input{${mainFile.name}}")
        }
        else if (this != TECTONIC || !mainFile.hasTectonicTomlFile()) {
            if (session.usesDefaultWorkingDirectory) {
                command.add(mainFile.name)
            }
            else {
                command.add(mainFile.path)
            }
        }

        return command
    }

    abstract fun createCommand(
        session: LatexRunSessionState,
        stepConfig: LatexCompileStepOptions,
        auxilPath: String?,
        outputPath: String,
        moduleRoots: Array<VirtualFile>,
    ): MutableList<String>

    open val includesBibtex = false

    open val handlesNumberOfCompiles = false

    open val outputFormats: Array<Format> = arrayOf(Format.PDF, Format.DVI)

    override fun toString() = this.displayName

    companion object {

        fun String.toWslPathIfNeeded(distributionType: LatexDistributionType): String =
            if (distributionType == LatexDistributionType.WSL_TEXLIVE) {
                runCommand("wsl", "wslpath", "-a", this) ?: this
            }
            else this
    }

    enum class Format {

        DEFAULT,
        PDF,
        DVI,
        HTML,
        XDV,
        AUX
    }
}
