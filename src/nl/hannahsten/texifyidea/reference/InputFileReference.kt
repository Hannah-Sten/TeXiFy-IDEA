package nl.hannahsten.texifyidea.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.findRootFile

/**
 * @author Abby Berkers
 */
class InputFileReference(element: LatexCommands, val range: TextRange) : PsiReferenceBase<LatexCommands>(element) {
    init {
        rangeInElement = range
    }

    val key by lazy {
        rangeInElement.substring(element.text)
    }

    override fun resolve(): PsiElement? {
        val root = element.containingFile.findRootFile().containingDirectory.virtualFile
        return PsiManager.getInstance(element.project)
                .findFile(root.findFile(key, Magic.File.includeExtensions) ?: return null)
    }
}