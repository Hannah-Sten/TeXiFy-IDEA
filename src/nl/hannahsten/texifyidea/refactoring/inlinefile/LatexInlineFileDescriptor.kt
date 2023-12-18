package nl.hannahsten.texifyidea.refactoring.inlinefile

import com.intellij.psi.PsiElement
import com.intellij.refactoring.RefactoringBundle
import com.intellij.usageView.UsageViewBundle
import com.intellij.usageView.UsageViewDescriptor
import nl.hannahsten.texifyidea.file.LatexFile

/**
 * Required file for the IDEA refactoring workflow. Parts have been borrowed from the java inline descriptor
 *
 * @see com.intellij.refactoring.inline.InlineViewDescriptor
 *
 * @author jojo2357
 */
class LatexInlineFileDescriptor(private val myElement: PsiElement) : UsageViewDescriptor {

    override fun getElements(): Array<PsiElement> {
        return arrayOf(myElement)
    }

    override fun getProcessedElementsHeader(): String {
        return if (this.myElement is LatexFile)
            "File to inline"
        else
            "Unknown element"
    }

    override fun getCodeReferencesText(usagesCount: Int, filesCount: Int): String {
        return RefactoringBundle.message(
            "invocations.to.be.inlined",
            UsageViewBundle.getReferencesString(usagesCount, filesCount)
        )
    }
}