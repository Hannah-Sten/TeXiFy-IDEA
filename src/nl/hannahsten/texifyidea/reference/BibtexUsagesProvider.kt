package nl.hannahsten.texifyidea.reference

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.tree.TokenSet
import nl.hannahsten.texifyidea.BibtexLexerAdapter
import nl.hannahsten.texifyidea.psi.BibtexTypes

class BibtexUsagesProvider : FindUsagesProvider {

    override fun getWordsScanner(): WordsScanner {
        return DefaultWordsScanner(
            BibtexLexerAdapter(),
            // Identifiers.
            TokenSet.create(
                BibtexTypes.ID, BibtexTypes.IDENTIFIER
            ),
            // Comments.
            TokenSet.create(BibtexTypes.COMMENT, BibtexTypes.COMMENT_TOKEN),
            // Literals.
            TokenSet.create(
                BibtexTypes.STRING, BibtexTypes.BRACED_STRING,
                BibtexTypes.QUOTED_STRING, BibtexTypes.DEFINED_STRING,
                BibtexTypes.NORMAL_TEXT_WORD, BibtexTypes.NORMAL_TEXT
            )
        )
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return element.node.text
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return element.text
    }

    override fun getType(element: PsiElement): String {
        return element.node.elementType.toString()
    }

    override fun getHelpId(psiElement: PsiElement): String? {
        return null
    }

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return psiElement is PsiNameIdentifierOwner
    }
}