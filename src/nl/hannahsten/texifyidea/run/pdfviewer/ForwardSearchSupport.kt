package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Editor
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

    /**
     * Checks if a viewer can be resolved and supports forward search.
     */
    fun canForwardSearch(project: Project, sourceFile: VirtualFile, fallback: PdfViewer? = null): Boolean {
        val viewer = resolveViewer(project, sourceFile, fallback) ?: return false
        return viewer.isAvailable() && viewer.isForwardSearchSupported
    }

    /**
     * Resolves the viewer and performs a forward search based on the position in the
     * given editor.
     *
     * @return the resolved viewer or `null` if no usable viewer got resolved
     * @throws nl.hannahsten.texifyidea.TeXception if the viewer reports a forward search failure
     */
    fun performForwardSearch(
        project: Project,
        file: VirtualFile,
        editor: Editor,
        fallbackViewer: PdfViewer? = null,
        focusAllowed: Boolean = true,
    ): PdfViewer? {
        val viewer = resolveViewer(project, file, fallbackViewer) ?: return null
        if (!viewer.isAvailable() || !viewer.isForwardSearchSupported) return null

        val document = editor.document
        val line = document.getLineNumber(editor.caretModel.offset) + 1
        val outputPath = resolveOutputPath(project, file)
        viewer.forwardSearch(outputPath, file.path, line, project, focusAllowed)
        return viewer
    }

    private fun resolveViewer(project: Project, sourceFile: VirtualFile, fallback: PdfViewer? = null): PdfViewer? = resolveRunConfig(project, sourceFile)?.pdfViewer
        ?: fallback
        ?: project.selectedRunConfig()?.pdfViewer
        ?: project.latexTemplateRunConfig()?.pdfViewer

    private fun resolveOutputPath(project: Project, sourceFile: VirtualFile): String? {
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
