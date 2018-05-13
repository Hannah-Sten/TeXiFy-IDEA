package nl.rubensten.texifyidea.run.compiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.rubensten.texifyidea.run.LatexRunConfiguration

/**
 * @author Sten Wessel
 */
internal object PdflatexMiktexCompiler : PdflatexBaseCompiler() {

    override val displayName = "pdfLaTeX (MiKTeX)"

    override fun getSpecificArguments(runConfig: LatexRunConfiguration, project: Project, moduleRoot: VirtualFile,
                                      moduleRoots: Array<VirtualFile>): List<String> {
        val arguments = mutableListOf<String>()

        arguments.apply {
            if (runConfig.hasAuxiliaryDirectories()) {
                add("-aux-directory=${moduleRoot.path}/auxil")
            }

            for (root in moduleRoots) {
                add("-include-directory=${root.path}")
            }
        }

        return arguments.toList()
    }
}
