package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexSemanticLookup

abstract class LatexPsiRecursiveWalker(depth: Int) : MyPsiRecursiveWalker(depth) {

    override fun elementStart(e: PsiElement) {
        if (e is LatexNormalText || e is PsiWhiteSpace) {
            // Skip normal text and whitespace elements
            goDown = false
        }
    }
}

//abstract class LatexWithContextVisitor(
//    semanticLookup: LatexSemanticLookup,depth: Int = Int.MAX_VALUE,
//    ) : MyPsiRecursiveWalker(depth) {
//
//    protected val currentContext : LContextSet
//
//    override fun elementStart(e: PsiElement) {
//
//
//    }
//}