package nl.hannahsten.texifyidea.run.compiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Companion.toWslPathIfNeeded
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil

/**
 * @author Sten Wessel
 */
internal object BibtexCompiler : Compiler<BibtexRunConfiguration> {

    override val displayName = "BibTeX"
    override val executableName = "bibtex"

    override fun getCommand(runConfig: BibtexRunConfiguration, project: Project): List<String>? {
        val command = mutableListOf<String>()

        val moduleRoots = ProjectRootManager.getInstance(project).contentSourceRoots

        command.apply {
            add(runConfig.compilerPath ?: executableName)

            runConfig.compilerArguments?.let { addAll(ParametersListUtil.parse(it)) }

            // Include files from auxiliary directory on Windows
            // We (mis)use project SDK as default setting for backwards compatibility
            if ((runConfig.getLatexDistributionType() == LatexDistributionType.PROJECT_SDK && LatexSdkUtil.isMiktexAvailable) || runConfig.getLatexDistributionType().isMiktex(project)) {
                val mainPath = runConfig.mainFile?.parent?.path?.toWslPathIfNeeded(runConfig.getLatexDistributionType()) ?: ""
                add("-include-directory=$mainPath")
                addAll(moduleRoots.map { "-include-directory=${it.path.toWslPathIfNeeded(runConfig.getLatexDistributionType())}" })
            }

            add(runConfig.mainFile?.nameWithoutExtension ?: return null)
        }

        return command.toList()
    }
}
