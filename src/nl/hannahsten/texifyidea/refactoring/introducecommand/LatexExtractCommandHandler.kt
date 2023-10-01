package nl.hannahsten.texifyidea.refactoring.introducecommand

import com.intellij.ide.plugins.PluginManagerCore.isUnitTestMode
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pass
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil.findCommonParent
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parents
import com.intellij.refactoring.IntroduceTargetChooser
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser
import com.intellij.refactoring.suggested.startOffset
import com.intellij.refactoring.util.CommonRefactoringUtil
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.psi.LatexTypes.NORMAL_TEXT_WORD
import nl.hannahsten.texifyidea.util.files.findExpressionAtCaret
import nl.hannahsten.texifyidea.util.files.findExpressionInRange
import nl.hannahsten.texifyidea.util.insertCommandDefinition
import nl.hannahsten.texifyidea.util.parser.*
import nl.hannahsten.texifyidea.util.runWriteCommandAction
import org.jetbrains.annotations.TestOnly

/**
 * Extract the selected piece of text into a \newcommand definition and replace usages.
 *
 * Based on code from https://github.com/intellij-rust/intellij-rust/blob/b18aab90317564307829f3c9c8e0188817a377ad/src/main/kotlin/org/rust/ide/refactoring/extraxtExpressionUi.kt#L1
 * and https://github.com/intellij-rust/intellij-rust/blob/b18aab90317564307829f3c9c8e0188817a377ad/src/main/kotlin/org/rust/ide/refactoring/extraxtExpressionUtils.kt#L1
 */
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
    }
    else
        IntroduceTargetChooser.showChooser(
            editor,
            exprs,
            callback.asPass,
            { it.text.substring(it.extractableIntRange) },
            RefactoringBundle.message("introduce.target.chooser.expressions.title"),
            { (it as LatexExtractablePSI).extractableRangeInFile }
        )
}

fun extractExpression(
    editor: Editor,
    expr: LatexExtractablePSI,
    commandName: String
) {
    if (!expr.isValid) return
    val occurrences = expr.findOccurrences()
    showOccurrencesChooser(editor, expr, occurrences) { occurrencesToReplace ->
        ExpressionReplacer(expr.project, editor, expr)
            .replaceElementForAllExpr(occurrencesToReplace, commandName)
    }
}

private class ExpressionReplacer(
    private val project: Project,
    private val editor: Editor,
    private val chosenExpr: LatexExtractablePSI
) {
    private val psiFactory = LatexPsiHelper(project)

    fun replaceElementForAllExpr(
        exprs: List<LatexExtractablePSI>,
        commandName: String
    ) {
        val containingFile = chosenExpr.containingFile
        runWriteCommandAction(project, commandName) {

            val letBinding = insertCommandDefinition(
                containingFile,
                chosenExpr.text.substring(chosenExpr.extractableIntRange)
            )
                ?: return@runWriteCommandAction
            exprs.filter { it != chosenExpr }.forEach {
                val newItem = it.text.replace(
                    chosenExpr.text.substring(chosenExpr.extractableIntRange),
                    "\\mycommand"
                )
                it.replace(psiFactory.createFromText(newItem).firstChild)
            }
            val newItem = chosenExpr.text.replace(
                chosenExpr.text.substring(chosenExpr.extractableIntRange),
                "\\mycommand"
            )
            chosenExpr.replace(psiFactory.createFromText(newItem).firstChild)

            val letOffset = letBinding.textRange

            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)

            println("you have beautiful eyes")

            val respawnedLetBinding = (containingFile as LatexFile).findExpressionAtCaret(letOffset.startOffset)
                ?: throw IllegalStateException("This really sux")

            val filterIsInstance =
                respawnedLetBinding.childrenOfType(PsiNamedElement::class).filterIsInstance<LatexCommands>()
            val actualToken =
                filterIsInstance.firstOrNull { it.text == "\\mycommand" }
                    ?: throw IllegalStateException("How did this happen??")

            editor.caretModel.moveToOffset(actualToken.textRange.startOffset)

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
    }
    else {
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

// Pass is deprecated, but IntroduceTargetChooser.showChooser doesnt have compatible signatures to replace with consumer yet
private val <T> ((T) -> Unit).asPass: Pass<T>
    get() = object : Pass<T>() {
        override fun pass(t: T) = this@asPass(t)
    }

fun findCandidateExpressionsToExtract(editor: Editor, file: LatexFile): List<LatexExtractablePSI> {
    val selection = editor.selectionModel
    if (selection.hasSelection()) {
        // If there's an explicit selection, suggest only one expression
        return listOfNotNull(file.findExpressionInRange(selection.selectionStart, selection.selectionEnd))
    }
    else {
        val expr = file.findExpressionAtCaret(editor.caretModel.offset)
            ?: return emptyList()
        if (expr is LatexBeginCommand) {
            val endCommand = expr.endCommand()
            return if (endCommand == null)
                emptyList()
            else {
                val environToken = findCommonParent(expr, endCommand)
                if (environToken != null)
                    listOf(environToken.asExtractable())
                else
                    emptyList()
            }
        }
        else if (expr is LatexNormalText) {
            return listOf(expr.asExtractable())
        }
        else {
            if (expr.elementType == NORMAL_TEXT_WORD) {
                val interruptedParent = expr.firstParentOfType(LatexNormalText::class)
                    ?: expr.firstParentOfType(LatexParameterText::class)
                    ?: throw IllegalStateException("You suck")
                val out = arrayListOf(expr.asExtractable())
                val interruptedText = interruptedParent.text
                if (interruptedText.contains('\n')) {
                    val previousLineBreak =
                        interruptedText.substring(0, editor.caretModel.offset - interruptedParent.startOffset)
                            .lastIndexOf('\n')
                    val startIndex = previousLineBreak + 1 + interruptedText.substring(previousLineBreak + 1)
                        .indexOfFirst { !it.isWhitespace() }
                    val nextNewlineindex = interruptedText.substring(startIndex).indexOf('\n')
                    val endOffset = if (nextNewlineindex == -1)
                        interruptedParent.textLength
                    else
                        startIndex + nextNewlineindex
                    out.add(interruptedParent.asExtractable(TextRange(startIndex, endOffset)))
                }

                val mathParent = expr.firstParentOfType(LatexInlineMath::class)
                if (mathParent != null) {
                    val mathChild = mathParent.firstChildOfType(LatexMathContent::class)
                    if (mathChild != null)
                        out.add(mathChild.asExtractable())
                    out.add(mathParent.asExtractable())
                }
                out.add(interruptedParent.asExtractable())
                return out.distinctBy { it.text.substring(it.extractableIntRange) }
            }
            else
                return expr.parents(true)
                    .takeWhile { it.elementType == NORMAL_TEXT_WORD || it is LatexNormalText || it is LatexParameter || it is LatexMathContent || it is LatexCommandWithParams }
                    .distinctBy { it.text }
                    .map { it.asExtractable() }
                    .toList()
        }
    }
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
    }
    finally {
        MOCK = null
    }
}
