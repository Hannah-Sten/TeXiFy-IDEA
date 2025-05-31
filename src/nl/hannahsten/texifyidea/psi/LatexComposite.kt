package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement

/**
 * This is a marker interface for PSI elements in LaTeX.
 * It is used to group elements that can contain other elements, such as environments or commands.
 */
interface LatexComposite : PsiElement