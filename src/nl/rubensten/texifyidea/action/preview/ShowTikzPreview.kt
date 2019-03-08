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

class ShowTikzPreview : PreviewAction("Tikz Picture Preview", TexifyIcons.EQUATION_PREVIEW) {
    override fun actionPerformed(file: VirtualFile, project: Project, editor: TextEditor) {
        val element: PsiElement = getElement(file, project, editor) ?: return

        val tikzEnvironment = findTikzEnvironment(element) ?: return

        displayPreview(project, tikzEnvironment, FORM_KEY) {
            preamble += "\\usepackage{tikz, pgfplots, amsmath}\n"

            val tikzLibs = PackageUtils.getIncludedTikzLibraries(getPsiFile(file, project) ?: return@displayPreview)
            preamble += "\\usetikzlibrary{${tikzLibs.joinToString()}}\n"

            val pgfLibs = PackageUtils.getIncludedPgfLibraries(getPsiFile(file, project) ?: return@displayPreview)
            preamble += "\\usepgfplotslibrary{${pgfLibs.joinToString()}}\n"

            waitTime = 5L
        }
    }

    private fun findTikzEnvironment(innerElement: PsiElement): PsiElement? {
        if (innerElement is LatexEnvironment && innerElement.isTikz()) return innerElement

        var currElement = innerElement.parentOfType(LatexEnvironment::class) ?: return null

        while (!currElement.isTikz() && currElement.hasParent(LatexEnvironment::class)) {
            currElement = currElement.parentOfType(LatexEnvironment::class)!!
        }

        return if (currElement.isTikz()) currElement else null
    }

    private fun LatexEnvironment.isTikz() = beginCommand.environmentName()?.toLowerCase() == "tikzpicture"

    companion object {
        @JvmStatic
        val FORM_KEY = Key<PreviewFormUpdater>("updater")
    }
}