package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexRequiredParam
import nl.hannahsten.texifyidea.util.endOffset
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.firstChildOfType
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.trimRange
import java.util.*

/**
 * @author Hannah Schellekens
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
            if (command.name !in CommandMagic.sectionMarkers) {
                continue
            }

            val sectionName = command.firstChildOfType(LatexRequiredParam::class)?.text?.trimRange(1, 1) ?: continue
            if (!PatternMagic.excessWhitespace.matcher(sectionName).matches()) {
                continue
            }

            val name = command.name ?: command.commandToken.text
            descriptors.add(
                manager.createProblemDescriptor(
                    command,
                    TextRange.from(name.length + 1, sectionName.length),
                    "Unnecessary whitespace",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly,
                    TrimFix()
                )
            )
        }

        return descriptors
    }

    /**
     * @author Hannah Schellekens
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
