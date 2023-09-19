package nl.hannahsten.texifyidea.refactoring.myextractfunction

import com.intellij.codeInsight.PsiEquivalenceUtil
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.Pass
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTreeUtil.findCommonParent
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parents
import com.intellij.refactoring.IntroduceTargetChooser
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.refactoring.util.CommonRefactoringUtil
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.psi.LatexTypes.NORMAL_TEXT_WORD
import nl.hannahsten.texifyidea.util.parser.childrenOfType
import nl.hannahsten.texifyidea.util.parser.firstChildOfType
import nl.hannahsten.texifyidea.util.runWriteCommandAction

class LatexExtractCommandHandler : RefactoringActionHandler {
    override fun invoke(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext?) {
        if (file !is LatexFile) return
        val exprs = findCandidateExpressionsToExtract(editor, file)

        if (exprs.isEmpty()) {
            val message = RefactoringBundle.message(
                if (editor.selectionModel.hasSelection())
                    "selected.block.should.represent.an.expression"
                else
                    "refactoring.introduce.selection.error"
            )
            val title = RefactoringBundle.message("introduce.variable.title")
            val helpId = "refactoring.extractVariable"
            CommonRefactoringUtil.showErrorHint(project, editor, message, title, helpId)
        }
        else {
            val extractor = { expr: PsiElement ->
                extractExpression(
                    editor, expr, ":)"//RsBundle.message("command.name.introduce.local.variable")
                )
            }
            if (exprs.size == 1) {
                extractor(exprs.single())
            } else showExpressionChooser(editor, exprs) {
                extractor(it)
            }

            /*else showExpressionChooser(editor, exprs) {
                extractor(it)
            }*/
        }
        /*
                val start = editor.selectionModel.selectionStart
                val end = editor.selectionModel.selectionEnd
                if (start === null || end === null) return

                // what do we need to do?
                */
        /*
                Resolve the current selection. If this is text, we need to ask to extract the current word, sentence, paragraph.
                If this is inter-environmental, we need to select all the environments.
                 *//*

        val firstUnresolved = file.findElementAt(start) ?: return
        val first =
            if (firstUnresolved is PsiWhiteSpace)
                file.findElementAt(firstUnresolved.startOffset - 1) ?: return
            else
                firstUnresolved

        val lastUnresolved = file.findElementAt(end - 1) ?: return
        val last =
            if (lastUnresolved is PsiWhiteSpace)
                file.findElementAt(lastUnresolved.endOffset) ?: return
            else
                lastUnresolved

        val parent = findCommonParent(first, last)

        // should be doing extra here?

        val psiSeq = generateSequence(first) {
            if (it.nextSibling == last)
                null
            else
                it.nextSibling
        }

        val entries = PsiUtilCore.toPsiElementArray(psiSeq.filter{ it !is PsiWhiteSpace}.toList())

        if (entries.isEmpty()) return

        println("I would have extracted " + entries.fold ("") { out, curr -> out + curr.text + " " });
*/

        /*
        We need to find other usages of this, so we can replace them too
         */

        /*
        We need to create the command calls
         */

        /*
        we need to settle on a name for those calls
         */
    }

    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
        TODO("This was not meant to happen like this")
    }
}

fun showExpressionChooser(
    editor: Editor,
    exprs: List<PsiElement>,
    callback: (PsiElement) -> Unit
) {
    IntroduceTargetChooser.showChooser(editor, exprs, callback.asPass) { it.text }
}

fun extractExpression(
    editor: Editor,
    expr: PsiElement,
    @Suppress("UnstableApiUsage")
    @NlsContexts.Command commandName: String
) {
    if (!expr.isValid) return
    val occurrences = findOccurrences(expr)
    showOccurrencesChooser(editor, expr, occurrences) { occurrencesToReplace ->
        ExpressionReplacer(expr.project, editor, expr)
            .replaceElementForAllExpr(occurrencesToReplace, commandName)
    }
}

private class ExpressionReplacer(
    private val project: Project,
    private val editor: Editor,
    private val chosenExpr: PsiElement
) {
    private val psiFactory = LatexPsiHelper(project)

    fun replaceElementForAllExpr(
        exprs: List<PsiElement>,
        @Suppress("UnstableApiUsage")
        @NlsContexts.Command commandName: String
    ) {
        val sortedExprs = exprs.sortedBy { it.startOffset }
        val firstExpr = sortedExprs.firstOrNull() ?: chosenExpr

        val newcommand = psiFactory.createFromText("\\newcommand{\\mycommand}{${chosenExpr.text}}").firstChild
        val name = psiFactory.createFromText("\\mycommand{}").firstChildOfType(LatexCommands::class) ?: return

        runWriteCommandAction(project, commandName) {
            val letBinding = firstExpr.parent.addBefore(newcommand, firstExpr)
            exprs.forEach { it.replace(name) }


            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)
            val actualToken =
                letBinding.childrenOfType(PsiNameIdentifierOwner::class).filterIsInstance<LatexCommands>()
                    .first { it.text == "\\mycommand" }

            editor.caretModel.moveToOffset(actualToken.textRange.startOffset)

            LatexInPlaceVariableIntroducer(
                actualToken, editor, project, "Choose me!"
            )
                .performInplaceRefactoring(LinkedHashSet())
        }
    }
}

fun showOccurrencesChooser(
    editor: Editor,
    expr: PsiElement,
    occurrences: List<PsiElement>,
    callback: (List<PsiElement>) -> Unit
) {
    OccurrencesChooser.simpleChooser<PsiElement>(editor)
        .showChooser(
            expr,
            occurrences,
            { choice: OccurrencesChooser.ReplaceChoice ->
                val toReplace = if (choice == OccurrencesChooser.ReplaceChoice.ALL) occurrences else listOf(expr)
                callback(toReplace)
            }.asPass
        )
}

private val <T> ((T) -> Unit).asPass: Pass<T>
    get() = object : Pass<T>() {
        override fun pass(t: T) = this@asPass(t)
    }

fun findExpressionInRange(file: PsiFile, startOffset: Int, endOffset: Int): PsiElement? {
    val firstUnresolved = file.findElementAt(startOffset) ?: return null
    val first =
        if (firstUnresolved is PsiWhiteSpace)
            file.findElementAt(firstUnresolved.startOffset - 1) ?: return null
        else
            firstUnresolved

    val lastUnresolved = file.findElementAt(endOffset - 1) ?: return null
    val last =
        if (lastUnresolved is PsiWhiteSpace)
            file.findElementAt(lastUnresolved.endOffset) ?: return null
        else
            lastUnresolved

    return findCommonParent(first, last)
}

fun findCandidateExpressionsToExtract(editor: Editor, file: LatexFile): List<PsiElement> {
    val selection = editor.selectionModel
    return if (selection.hasSelection()) {
        // If there's an explicit selection, suggest only one expression
        listOfNotNull(findExpressionInRange(file, selection.selectionStart, selection.selectionEnd))
    }
    else {
        val expr = findExpressionAtCaret(file, editor.caretModel.offset)
            ?: return emptyList()
        // Finds possible expressions that might want to be bound to a local variable.
        // We don't go further than the current block scope,
        // further more path expressions don't make sense to bind to a local variable so we exclude them.
        expr.parents(true)
            .takeWhile { it.elementType == NORMAL_TEXT_WORD || it is LatexNormalText || it is LatexParameter || it is LatexMathContent || it is LatexCommandWithParams }
            .toList()
    }
}

fun findExpressionAtCaret(file: LatexFile, offset: Int): PsiElement? {
    val expr = file.expressionAtOffset(offset)
    val exprBefore = file.expressionAtOffset(offset - 1)
    return when {
        expr == null -> exprBefore
        exprBefore == null -> expr
        PsiTreeUtil.isAncestor(expr, exprBefore, false) -> exprBefore
        else -> expr
    }
}

fun LatexFile.expressionAtOffset(offset: Int): PsiElement? {
    val element = findElementAt(offset) ?: return null

    return element.parents(true)
        .firstOrNull { it.elementType == NORMAL_TEXT_WORD || it is LatexNormalText || it is LatexParameter || it is LatexMathContent || it is LatexCommandWithParams }
}

/**
 * Finds occurrences in the sub scope of expr, so that all will be replaced if replace all is selected.
 */

fun findOccurrences(expr: PsiElement): List<PsiElement> {
    val parent = expr.parent
        ?: return emptyList()
    return findOccurrences(parent, expr)
}

fun findOccurrences(parent: PsiElement, expr: PsiElement): List<PsiElement> {
    val visitor = object : PsiRecursiveElementVisitor() {
        val foundOccurrences = ArrayList<PsiElement>()
        override fun visitElement(element: PsiElement) {
            if (PsiEquivalenceUtil.areElementsEquivalent(expr, element)) {
                foundOccurrences.add(element)
            }
            else {
                super.visitElement(element)
            }
        }
    }
    parent.acceptChildren(visitor)
    return visitor.foundOccurrences
}

/*
fun moveEditorToNameElement(editor: Editor, element: PsiElement?): RsPatBinding? {
    val newName = element?.findBinding()
    editor.caretModel.moveToOffset(newName?.identifier?.textRange?.startOffset ?: 0)
    return newName
}

fun PsiElement.findBinding() = PsiTreeUtil.findChildOfType(this,::class.java)
*/
