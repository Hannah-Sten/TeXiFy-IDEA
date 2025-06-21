package nl.hannahsten.texifyidea.refactoring.introducecommand

import com.intellij.ide.plugins.PluginManagerCore.isUnitTestMode
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pass
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil.findCommonParent
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parents
import com.intellij.refactoring.IntroduceTargetChooser
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser
import com.intellij.psi.util.startOffset
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

        // almost never happens, so the error will be likely worded wrong, but hopefully that will generate more bug reports!
        if (exprs.isEmpty()) {
            val message = RefactoringBundle.message(
                if (editor.selectionModel.hasSelection())
                    "selected.block.should.represent.an.expression"
                else
                    "refactoring.introduce.selection.error"
            )
            val title = "Introduce Custom Command"
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
            // if there are multiple candidates (ie the user did not have an active selection, ask for them to choose what to extract
            else showExpressionChooser(editor, exprs) {
                extractor(it)
            }
        }
    }

    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) { }
}

fun showExpressionChooser(
    editor: Editor,
    candidates: List<LatexExtractablePSI>,
    callback: (LatexExtractablePSI) -> Unit
) {
    if (isUnitTestMode) {
        callback(MOCK!!.chooseTarget(candidates))
    }
    else
        IntroduceTargetChooser.showChooser(
            editor,
            candidates,
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

    /**
     * This actually replaces all the ocurrences
     */
    fun replaceElementForAllExpr(
        exprs: List<LatexExtractablePSI>,
        commandName: String
    ) {
        // cache file in case the psi tree breaks
        val containingFile = chosenExpr.containingFile
        runWriteCommandAction(project, commandName) {
            val definitionToken = insertCommandDefinition(
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

            val definitionOffset = definitionToken.textRange

            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)

            // sometimes calling the previous line will invalidate `definitionToken`, so we will make sure to find the actual valid token
            val vampireCommandDefinition = containingFile.findExpressionAtCaret(definitionOffset.startOffset)
                ?: throw IllegalStateException("Unexpectedly could not find an expression")

            val actualToken =
                vampireCommandDefinition.traverseTyped<LatexCommands>()
                    .firstOrNull { it.text == "\\mycommand" }
                    ?: throw IllegalStateException("Psi Tree was not in the expected state")

            editor.caretModel.moveToOffset(actualToken.textRange.startOffset)

            // unsure where title is used. Either way, put the user into a refactor where they get to specify the new command name
            LatexInPlaceVariableIntroducer(
                actualToken, editor, project, "Choose a Variable"
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

/**
 * Returns a list of "expressions" which could be extracted.
 */
fun findCandidateExpressionsToExtract(editor: Editor, file: LatexFile): List<LatexExtractablePSI> {
    val selection = editor.selectionModel
    // if the user has highlighted a block, simply return that
    if (selection.hasSelection()) {
        // If there's an explicit selection, suggest only one expression
        return listOfNotNull(file.findExpressionInRange(selection.selectionStart, selection.selectionEnd))
    }
    else {
        val expr = file.findExpressionAtCaret(editor.caretModel.offset)
            ?: return emptyList()
        // if expr is a \begin, return the whole block it is a part of, and just assume since the cursor was there that it was meant to be
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
        // if this was text, like in a command parameter, only ofer itself
        else if (expr is LatexNormalText) {
            return listOf(expr.asExtractable())
        }
        else {
            // if inside a text block, we will offer the current word, current sentence, current line, whole block, and applicable parents
            if (expr.elementType == NORMAL_TEXT_WORD) {
                // variable where we will build up our return
                val out = arrayListOf(expr.asExtractable())

                val interruptedParent = expr.firstParentOfType(LatexNormalText::class)
                    ?: expr.firstParentOfType(LatexParameterText::class)
                    ?: return emptyList()
                val interruptedText = interruptedParent.text
                // in this text block, if it multiline, find current line
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

                // if this text is in a math context, offer the math environ
                val mathParent = expr.firstParentOfType(LatexInlineMath::class)
                if (mathParent != null) {
                    val mathChild = mathParent.findFirstChildOfType(LatexMathContent::class)
                    if (mathChild != null)
                        out.add(mathChild.asExtractable())
                    out.add(mathParent.asExtractable())
                }
                out.add(interruptedParent.asExtractable())
                return out.distinctBy { it.text.substring(it.extractableIntRange) }
            }
            // default behavior: offer to extract any parent that we consider "extractable"
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

// This allows us to run tests and mimic user input
@Suppress("ktlint:standard:property-naming")
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
