package nl.hannahsten.texifyidea.refactoring.introduceCommand

import com.intellij.codeInsight.PsiEquivalenceUtil
import com.intellij.ide.plugins.PluginManagerCore.isUnitTestMode
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.Pass
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTreeUtil.findCommonParent
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
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
import nl.hannahsten.texifyidea.util.insertCommandDefinition
import nl.hannahsten.texifyidea.util.parser.*
import nl.hannahsten.texifyidea.util.runWriteCommandAction
import org.jetbrains.annotations.TestOnly

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
            val extractor = { expr: LatexExtractablePSI ->
                extractExpression(
                    editor, expr, RefactoringBundle.message("introduce.variable.title")
                )
            }
            if (exprs.size == 1) {
                extractor(exprs.single())
            }
            else showExpressionChooser(editor, exprs) {
                extractor(it)
            }
        }
    }

    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
        TODO("This was not meant to happen like this")
    }
}

fun showExpressionChooser(
    editor: Editor,
    exprs: List<LatexExtractablePSI>,
    callback: (LatexExtractablePSI) -> Unit
) {
    if (isUnitTestMode) {
        callback(MOCK!!.chooseTarget(exprs))
    } else
        IntroduceTargetChooser.showChooser(editor, exprs, callback.asPass) { it.text.substring(it.extractableRange.startOffset, it.extractableRange.endOffset) }
}

fun extractExpression(
    editor: Editor,
    expr: LatexExtractablePSI,
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

        val name = psiFactory.createFromText("\\mycommand{}").firstChildOfType(LatexCommands::class) ?: return

        runWriteCommandAction(project, commandName) {
            val letBinding = insertCommandDefinition(chosenExpr.containingFile, chosenExpr.text)
                ?: return@runWriteCommandAction //firstExpr.parent.addBefore(newcommand, firstExpr)
            exprs.filter{ it != chosenExpr }.forEach { it.replace(name) }
            val chosenInsertion = chosenExpr.replace(name)

            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)
            val filterIsInstance =
                letBinding.childrenOfType(PsiNamedElement::class).filterIsInstance<LatexCommands>()
            val actualToken =
                filterIsInstance.firstOrNull { it.text == "\\mycommand" }
                    ?: throw IllegalStateException("How did this happen??")

            editor.caretModel.moveToOffset(chosenInsertion.textRange.startOffset)

            LatexInPlaceVariableIntroducer(
                actualToken, editor, project, "choose a variable"
            )
                .performInplaceRefactoring(LinkedHashSet())
        }
    }
}

fun showOccurrencesChooser(
    editor: Editor,
    expr: LatexExtractablePSI,
    occurrences: List<LatexExtractablePSI>,
    callback: (List<LatexExtractablePSI>) -> Unit
) {
    if (isUnitTestMode && occurrences.size > 1) {
        callback(MOCK!!.chooseOccurrences(expr, occurrences))
    } else {
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
}

private val <T> ((T) -> Unit).asPass: Pass<T>
    get() = object : Pass<T>() {
        override fun pass(t: T) = this@asPass(t)
    }

fun findExpressionInRange(file: PsiFile, startOffset: Int, endOffset: Int): LatexExtractablePSI? {
    val firstUnresolved = file.findElementAt(startOffset) ?: return null
    val first =
        if (firstUnresolved is PsiWhiteSpace)
            file.findElementAt(firstUnresolved.endOffset) ?: return null
        else
            firstUnresolved

    val lastUnresolved = file.findElementAt(endOffset - 1) ?: return null
    val last =
        if (lastUnresolved is PsiWhiteSpace)
            file.findElementAt(lastUnresolved.startOffset - 1) ?: return null
        else
            lastUnresolved

    val parent = findCommonParent(first, last) ?: return null

    return if (parent is LatexNormalText) {
        LatexExtractablePSI(parent, TextRange(startOffset, endOffset))
    } else
        LatexExtractablePSI(parent)
}

fun findCandidateExpressionsToExtract(editor: Editor, file: LatexFile): List<LatexExtractablePSI> {
    val selection = editor.selectionModel
    if (selection.hasSelection()) {
        // If there's an explicit selection, suggest only one expression
        return listOfNotNull(findExpressionInRange(file, selection.selectionStart, selection.selectionEnd))
    }
    else {
        val expr = findExpressionAtCaret(file, editor.caretModel.offset)
            ?: return emptyList()
        if (expr is LatexBeginCommand) {
            val endCommand = expr.endCommand()
            if (endCommand == null)
                return emptyList()
            else {
                val environToken = findCommonParent(expr, endCommand)
                return if (environToken != null)
                    listOf(LatexExtractablePSI(environToken))
                else
                    emptyList()
            }
        } else if (expr is LatexNormalText) {
            return listOf(LatexExtractablePSI(expr))
        } else {
            if (expr.elementType == NORMAL_TEXT_WORD) {
                val interruptedParent = expr.firstParentOfType(LatexNormalText::class)
                    ?: throw IllegalStateException("You suck")
                var out = arrayListOf(LatexExtractablePSI(expr), LatexExtractablePSI(interruptedParent))
                if (interruptedParent.text.contains('\n')) {
                    val previousLineBreak = interruptedParent.text.substring(0, editor.caretModel.offset - interruptedParent.startOffset).lastIndexOf('\n')
                    val startIndex = previousLineBreak + 1 + interruptedParent.text.substring(previousLineBreak + 1).indexOfFirst { """\S""".toRegex().matches(it.toString()) }
                    val nextNewlineindex = interruptedParent.text.substring(startIndex).indexOf('\n')
                    val endOffset = if (nextNewlineindex == -1)
                        interruptedParent.textLength
                    else
                        startIndex + nextNewlineindex
                    out.add(1, LatexExtractablePSI(interruptedParent, TextRange(startIndex, endOffset)))
                }
                return out
            }
            return expr.parents(true)
                .takeWhile { it.elementType == NORMAL_TEXT_WORD || it is LatexNormalText || it is LatexParameter || it is LatexMathContent || it is LatexCommandWithParams }
                .distinctBy { it.text }
                .map { LatexExtractablePSI(it) }
                .toList()
        }
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

/**
 * Gets the smallest extractable expression at the given offset
 */
fun LatexFile.expressionAtOffset(offset: Int): PsiElement? {
    val element = findElementAt(offset) ?: return null

    return element.parents(true)
        .firstOrNull { it.elementType == NORMAL_TEXT_WORD || it is LatexNormalText || it is LatexParameter || it is LatexMathContent || it is LatexCommandWithParams }
}

/**
 * Finds occurrences in the sub scope of expr, so that all will be replaced if replace all is selected.
 */
fun findOccurrences(expr: PsiElement): List<LatexExtractablePSI> {
    val parent = expr.parentOfType(LatexFile::class)
        ?: return emptyList()
    return findOccurrences(parent, expr)
}

fun findOccurrences(parent: PsiElement, expr: PsiElement): List<LatexExtractablePSI> {
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
    return visitor.foundOccurrences.map { LatexExtractablePSI(it) }
}

interface ExtractExpressionUi {
    fun chooseTarget(exprs: List<LatexExtractablePSI>): LatexExtractablePSI
    fun chooseOccurrences(expr: LatexExtractablePSI, occurrences: List<LatexExtractablePSI>): List<LatexExtractablePSI>
}

var MOCK: ExtractExpressionUi? = null
@TestOnly
fun withMockTargetExpressionChooser(mock: ExtractExpressionUi, f: () -> Unit) {
    MOCK = mock
    try {
        f()
    } finally {
        MOCK = null
    }
}
