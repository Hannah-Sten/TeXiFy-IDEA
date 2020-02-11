package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.psi.impl.LatexEnvironmentImpl

class LatexEnvironmentManipulator : AbstractElementManipulator<LatexEnvironmentImpl>() {

  override fun handleContentChange(element: LatexEnvironmentImpl,
                                   range: TextRange,
                                   newContent: String): LatexEnvironmentImpl? {
    val oldText = element.text
    val newText = oldText.substring(0, range.startOffset) +
        newContent +
        oldText.substring(range.endOffset)
    val file = PsiFileFactory.getInstance(element.project)
        .createFileFromText("temp.tex", LatexLanguage.INSTANCE, newText)
    val res =
        PsiTreeUtil.findChildOfType(file, LatexEnvironmentImpl::class.java) ?: return null
    element.replace(res)
    return element
  }

}



