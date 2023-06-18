package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInsight.intention.FileModifier.SafeFieldForPreview
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.webSymbols.references.WebSymbolReferenceProvider.Companion.startOffsetIn
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.psi.LatexRequiredParam
import nl.hannahsten.texifyidea.util.containsAny
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.firstChildOfType
import nl.hannahsten.texifyidea.util.parser.requiredParameter

open class LatexSuspiciousSectionFormattingInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    val formatting = setOf("~", "\\\\")

    override fun getDisplayName() = "Suspicious formatting in the required argument of a sectioning command"

    override val inspectionId = "SuspiciousSectionFormatting"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        return file.commandsInFile()
            .asSequence()
            .filter { it.name in CommandMagic.sectionMarkers }
            .filter { it.parameterList.mapNotNull { param -> param.optionalParam }.isEmpty() }
            .filter { it.requiredParameter(0)?.containsAny(formatting) == true }
            .map { psiElement ->
                val requiredParam = psiElement.firstChildOfType(LatexRequiredParam::class)
                // Plus 1 for the opening brace.
                val startOffset = requiredParam?.startOffsetIn(psiElement)?.plus(1) ?: 0
                // Minus 2 for the braces surrounding the parameter.
                val endOffset = requiredParam?.textLength?.minus(2)?.plus(startOffset) ?: psiElement.textLength
                manager.createProblemDescriptor(
                    psiElement,
                    TextRange(startOffset, endOffset),
                    "Suspicious formatting in ${psiElement.name}",
                    ProblemHighlightType.WARNING,
                    isOntheFly,
                    AddOptionalArgumentQuickFix(formatting)
                )
            }
            .toList()
    }

    class AddOptionalArgumentQuickFix(@SafeFieldForPreview val formatting: Set<String>) : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Fix formatting in table of contents and running head"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val requiredParamText = command.requiredParameter(0)
            val optionalParamText = requiredParamText?.replace(Regex(formatting.joinToString("", prefix = "[", postfix = "]")), " ")
                ?: return
            val optionalArgument = LatexPsiHelper(project).createOptionalParameter(optionalParamText) ?: return

            command.addAfter(optionalArgument, command.commandToken)
            // Create a new command and completely replace the old command so all the psi methods will recompute instead
            // of using old values from their cache.
            val newCommand = LatexPsiHelper(project).createFromText(command.text).firstChildOfType(LatexCommands::class)
                ?: return
            command.replace(newCommand)
        }
    }
}