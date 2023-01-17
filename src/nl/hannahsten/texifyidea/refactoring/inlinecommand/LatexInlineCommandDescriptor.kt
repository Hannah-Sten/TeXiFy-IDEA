package nl.hannahsten.texifyidea.refactoring.inlinecommand

import com.intellij.java.refactoring.JavaRefactoringBundle
import com.intellij.psi.PsiElement
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
class LatexInlineCommandDescriptor(private val myElement: PsiElement) : UsageViewDescriptor {

    override fun getElements(): Array<PsiElement> {
        return arrayOf(myElement)
    }

    override fun getProcessedElementsHeader(): String {
        return if (this.myElement is LatexFile)
            "Command to inline"
        else
            "Unknown element"
    }

    override fun getCodeReferencesText(usagesCount: Int, filesCount: Int): String {
        return JavaRefactoringBundle.message(
            "invocations.to.be.inlined",
            UsageViewBundle.getReferencesString(usagesCount, filesCount)
        )
    }
}