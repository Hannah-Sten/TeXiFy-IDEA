package nl.hannahsten.texifyidea.action.preview

import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.PackageUtils
import nl.hannahsten.texifyidea.util.parser.environmentName
import nl.hannahsten.texifyidea.util.parser.hasParent
import nl.hannahsten.texifyidea.util.parser.parentOfType
import java.util.*

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

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        val element: PsiElement = getElement(file, project, textEditor)
            ?: return

        // Make sure we're currently in a tikz environment.
        val tikzEnvironment = findTikzEnvironment(element) ?: return

        // jlatexmath cannot display tikz
        displayPreview(project, tikzEnvironment, FORM_KEY, canUseJlatexmath = false) {
            resetPreamble()
            val psiFile = getPsiFile(file, project) ?: return@displayPreview

            preamble += "\\usepackage{tikz, pgfplots, amsmath}\n"

            // Add all the tikz libs included in related packages (via \\usetikzlibrary{}) to the produced document.
            val tikzLibs = PackageUtils.getIncludedTikzLibraries(psiFile)
            if (tikzLibs.isNotEmpty()) {
                userPreamble += "\\usetikzlibrary{${tikzLibs.joinToString()}}\n"
            }

            // Add all the pgfplots libs included in related packages (via \\usepgfplotslibrary{}) to the produced document.
            val pgfLibs = PackageUtils.getIncludedPgfLibraries(psiFile)
            if (pgfLibs.isNotEmpty()) {
                userPreamble += "\\usepgfplotslibrary{${pgfLibs.joinToString()}}\n"
            }

            userPreamble += findPreamblesFromMagicComments(psiFile, "tikz")
            waitTime = 5L
        }
    }

    fun findTikzEnvironment(innerElement: PsiElement): PsiElement? {
        // If the selected element is already a tikz env, we're good.
        if (innerElement is LatexEnvironment && innerElement.isTikz()) return innerElement

        // Find the first LatexEnvironment parent. If there are none, we aren't in tikz.
        var currElement = innerElement.parentOfType(LatexEnvironment::class)
            ?: return null

        // Continue this process until we find a tikz environment or run out of parent environments.
        while (!currElement.isTikz() && currElement.hasParent(LatexEnvironment::class)) {
            currElement = currElement.parentOfType(LatexEnvironment::class)!!
        }

        // Finally, decide whether the outermost environment is tikz.
        return if (currElement.isTikz()) currElement else null
    }

    private fun LatexEnvironment.isTikz() = beginCommand.environmentName()
        ?.lowercase(Locale.getDefault()) == "tikzpicture"
}