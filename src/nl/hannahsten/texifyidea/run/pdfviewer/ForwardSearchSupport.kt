package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.LatexPathResolver
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport
import nl.hannahsten.texifyidea.util.files.findRootFiles
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
import nl.hannahsten.texifyidea.util.latexTemplateRunConfig
import nl.hannahsten.texifyidea.util.selectedRunConfig

internal object ForwardSearchSupport {

    fun resolveViewer(project: Project, sourceFile: VirtualFile, fallback: PdfViewer? = null): PdfViewer? = resolveRunConfig(project, sourceFile)?.pdfViewer
        ?: fallback
        ?: project.selectedRunConfig()?.pdfViewer
        ?: project.latexTemplateRunConfig()?.pdfViewer

    fun resolveOutputPath(project: Project, sourceFile: VirtualFile): String? {
        val runConfig = resolveRunConfig(project, sourceFile) ?: return null
        val mainFile = LatexRunConfigurationStaticSupport.resolveMainFile(runConfig) ?: return null
        val outputDir = LatexPathResolver.resolveOutputDir(runConfig, mainFile) ?: return null
        return "${outputDir.path}/${mainFile.nameWithoutExtension}.pdf"
    }

    fun sourceBelongsToMainFileset(project: Project, sourceFile: VirtualFile, mainFile: VirtualFile): Boolean {
        if (sourceFile == mainFile) {
            return true
        }
        return ReadAction.computeBlocking<Boolean, RuntimeException> {
            val sourcePsi = sourceFile.psiFile(project) ?: return@computeBlocking false
            sourcePsi.findRootFiles().any { it.virtualFile == mainFile }
        }
    }

    private fun resolveRunConfig(project: Project, sourceFile: VirtualFile): LatexRunConfiguration? {
        val selected = project.selectedRunConfig()
        if (selected != null && belongsToRunConfiguration(project, sourceFile, selected)) {
            return selected
        }

        return project.getLatexRunConfigurations().firstOrNull { runConfig ->
            belongsToRunConfiguration(project, sourceFile, runConfig)
        }
    }

    private fun belongsToRunConfiguration(project: Project, sourceFile: VirtualFile, runConfig: LatexRunConfiguration): Boolean {
        val mainFile = LatexRunConfigurationStaticSupport.resolveMainFile(runConfig) ?: return false
        return sourceBelongsToMainFileset(project, sourceFile, mainFile)
    }
}
