package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.psi.impl.LatexEnvironmentImpl

/**
 * Enable editing environment content in a separate window (when using language injection).
 */
class LatexEnvironmentManipulator : AbstractElementManipulator<LatexEnvironmentImpl>() {

    override fun handleContentChange(
        element: LatexEnvironmentImpl,
        range: TextRange,
        newContent: String
    ): LatexEnvironmentImpl? {
        val oldText = element.text
        // For some reason the endoffset of the given range is incorrect: sometimes it excludes the last line, so we calculate it ourselves
        val endOffset = oldText.indexOf("\\end{${element.environmentName}}") - 1 // -1 to exclude \n
        val newText = oldText.substring(0, range.startOffset) + newContent + oldText.substring(endOffset)
        val file = PsiFileFactory.getInstance(element.project)
            .createFileFromText("temp.tex", LatexLanguage, newText)
        val res =
            PsiTreeUtil.findChildOfType(file, LatexEnvironmentImpl::class.java) ?: return null
        element.replace(res)
        return element
    }
}
