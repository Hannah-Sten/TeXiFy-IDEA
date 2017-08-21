package nl.rubensten.texifyidea.util

import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexMathContent
import kotlin.reflect.KClass

/**
 * Get the offset where the psi element ends.
 */
fun PsiElement.endOffset(): Int = textOffset + textLength

/**
 * @see [PsiTreeUtil.getChildrenOfType]
 */
fun <T : PsiElement> PsiElement.childrenOfType(clazz: KClass<T>): Collection<T> = PsiTreeUtil.findChildrenOfType(this, clazz.java)

/**
 * @see [PsiTreeUtil.getParentOfType]
 */
fun <T : PsiElement> PsiElement.parentOfType(clazz: KClass<T>): T? = PsiTreeUtil.getParentOfType(this, clazz.java)

/**
 * Checks if the psi element has a parent of a given class.
 */
fun <T : PsiElement> PsiElement.hasParent(clazz: KClass<T>): Boolean = parentOfType(clazz) != null

/**
 * Checks if the psi element is in math mode or not.
 *
 * @return `true` when the element is in math mode, `false` when the element is in no math mode.
 */
fun PsiElement.inMathMode(): Boolean = hasParent(LatexMathContent::class)

/**
 * Get the corresponding document of the PsiFile.
 */
fun PsiFile.document(): Document? = PsiDocumentManager.getInstance(project).getDocument(this)

/**
 * @see [LatexCommandsIndex.getIndexCommands]
 */
fun PsiFile.commandsInFile(): Collection<LatexCommands> = LatexCommandsIndex.getIndexCommands(this)

/**
 * @see [LatexCommandsIndex.getIndexCommandsInFileSet]
 */
fun PsiFile.commandsInFileSet(): Collection<LatexCommands> = LatexCommandsIndex.getIndexCommandsInFileSet(this)