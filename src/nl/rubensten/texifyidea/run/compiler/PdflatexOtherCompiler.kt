package nl.rubensten.texifyidea.run.compiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.rubensten.texifyidea.run.LatexRunConfiguration

/**
 * @author Sten Wessel
 */
internal object PdflatexOtherCompiler : PdflatexBaseCompiler() {

    override val displayName = "pdfLaTeX (other distributions)"

    override fun getSpecificArguments(runConfig: LatexRunConfiguration, project: Project, moduleRoot: VirtualFile,
                                      moduleRoots: Array<VirtualFile>): List<String> {
        // See https://tex.stackexchange.com/questions/93712/definition-of-the-texinputs-variable
        environment["TEXINPUTS"] = moduleRoots.joinToString(separator = ":", prefix = ".:", postfix = ":") { it.path }
        return emptyList()
    }
}
