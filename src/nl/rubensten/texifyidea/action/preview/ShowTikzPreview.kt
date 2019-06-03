package nl.rubensten.texifyidea.action.preview

import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.LatexEnvironment
import nl.rubensten.texifyidea.ui.PreviewFormUpdater
import nl.rubensten.texifyidea.util.PackageUtils
import nl.rubensten.texifyidea.util.environmentName
import nl.rubensten.texifyidea.util.hasParent
import nl.rubensten.texifyidea.util.parentOfType

/**
 * The [ShowTikzPreview] class describes an Action in the editor that will display a rendered
 * TikZ picture using the [PreviewAction] class as a base.
 *
 * @author FalseHonesty
 */
class ShowTikzPreview : PreviewAction("Tikz Picture Preview", TexifyIcons.TIKZ_PREVIEW) {

    companion object {

        @JvmStatic
        val FORM_KEY = Key<PreviewFormUpdater>("updater")
    }

    override fun actionPerformed(file: VirtualFile, project: Project, editor: TextEditor) {
        val element: PsiElement = getElement(file, project, editor) ?: return

        // Make sure we're currently in a tikz environment.
        val tikzEnvironment = findTikzEnvironment(element) ?: return

        displayPreview(project, tikzEnvironment, FORM_KEY) {
            preamble += "\\usepackage{tikz, pgfplots, amsmath}\n"

            // Add all of the tikz libs included in related packages (via \usetikzlibrary{}) to the produced document.
            val tikzLibs = PackageUtils.getIncludedTikzLibraries(getPsiFile(file, project) ?: return@displayPreview)
            preamble += "\\usetikzlibrary{${tikzLibs.joinToString()}}\n"

            // Add all of the pgfplots libs included in related packages (via \usepgfplotslibrary{}) to the produced document.
            val pgfLibs = PackageUtils.getIncludedPgfLibraries(getPsiFile(file, project) ?: return@displayPreview)
            preamble += "\\usepgfplotslibrary{${pgfLibs.joinToString()}}\n"

            waitTime = 5L
        }
    }

    private fun findTikzEnvironment(innerElement: PsiElement): PsiElement? {
        // If the selected element is already a tikz env, we're good.
        if (innerElement is LatexEnvironment && innerElement.isTikz()) return innerElement

        // Find the first LatexEnvironment parent. If there are none, we aren't in tikz.
        var currElement = innerElement.parentOfType(LatexEnvironment::class) ?: return null

        // Continue this process until we find a tikz environment or run out of parent environments.
        while (!currElement.isTikz() && currElement.hasParent(LatexEnvironment::class)) {
            currElement = currElement.parentOfType(LatexEnvironment::class)!!
        }

        // Finally, decide whether or not the outermost environment is tikz.
        return if (currElement.isTikz()) currElement else null
    }

    private fun LatexEnvironment.isTikz() = beginCommand.environmentName()?.toLowerCase() == "tikzpicture"
}