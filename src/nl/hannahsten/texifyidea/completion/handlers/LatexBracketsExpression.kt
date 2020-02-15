package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.PsiElementResult
import com.intellij.codeInsight.template.Result
import com.intellij.psi.PsiDocumentManager
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.LatexPsiHelper


class LatexBracketsExpression(val command: LatexCommands, val nBrackets: Int) : Expression() {
    override fun calculateQuickResult(context: ExpressionContext?): Result? {
        val project = context?.project ?: return null
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        if (nBrackets <= 0) return null

        return PsiElementResult(LatexPsiHelper(command.project).createCommandWithRequiredParameters(command.name ?: return null, nBrackets))
    }

    override fun calculateLookupItems(context: ExpressionContext?): Array<LookupElement>? {
        if (nBrackets <= 0) return null
        val project = context?.project ?: return null
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        return arrayOf(LookupElementBuilder.create(command))
    }

    override fun calculateResult(context: ExpressionContext?): Result? {
        return calculateQuickResult(context)
    }
}