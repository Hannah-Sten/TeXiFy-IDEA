package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.lang.magic.MagicCommentScope
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexRequiredParam
import nl.rubensten.texifyidea.util.*
import java.util.*

/**
 * @author Ruben Schellekens
 */
open class LatexTrimWhitespaceInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "TrimWhitespace"

    override val ignoredSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Unnecessary whitespace in section commands"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = file.commandsInFile()
        for (command in commands) {
            if (command.name !in Magic.Command.sectionMarkers) {
                continue
            }

            val sectionName = command.firstChildOfType(LatexRequiredParam::class)?.group?.text?.trimRange(1, 1) ?: continue
            if (!Magic.Pattern.excessWhitespace.matcher(sectionName).matches()) {
                continue
            }

            val name = command.name ?: command.commandToken.text
            descriptors.add(manager.createProblemDescriptor(
                    command,
                    TextRange.from(name.length + 1, sectionName.length),
                    "Unnecessary whitespace",
                    ProblemHighlightType.WEAK_WARNING,
                    isOntheFly,
                    TrimFix()
            ))
        }

        return descriptors
    }

    /**
     * @author Ruben Schellekens
     */
    private class TrimFix : LocalQuickFix {

        override fun getFamilyName() = "Trim whitespace"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val document = command.containingFile.document() ?: return
            val param = command.firstChildOfType(LatexRequiredParam::class) ?: return

            val start = param.textOffset + 1
            val end = param.endOffset() - 1

            val newString = document.getText(TextRange(start, end)).trim()
            document.replaceString(start, end, newString)
        }
    }
}