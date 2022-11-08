package nl.hannahsten.texifyidea.refactoring.inlinefile

import com.intellij.java.refactoring.JavaRefactoringBundle
import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageViewBundle
import com.intellij.usageView.UsageViewDescriptor
import nl.hannahsten.texifyidea.file.LatexFile

class LatexInlineFileDescriptor(private val myElement: PsiElement): UsageViewDescriptor {

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
        return JavaRefactoringBundle.message(
            "invocations.to.be.inlined",
            UsageViewBundle.getReferencesString(usagesCount, filesCount)
        )
    }
}