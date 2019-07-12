package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.*
import java.util.*

/**
 * See [LatexNoExtensionInspection].
 */
open class LatexRequiredExtensionInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "RequiredExtension"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "File argument should include the extension"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        LatexCommandsIndex.getItems(file).asSequence()
                .filter { it.name in Magic.Command.requiredExtensions }
                .filter { command ->
                    Magic.Command.requiredExtensions[command.name]!!.any { command.requiredParameter(0)?.endsWith(it) == false }
                }
                .forEach {
                    descriptors.add(manager.createProblemDescriptor(
                            it,
                            TextRange.allOf(it.requiredParameter(0)!!).shiftRight(it.commandToken.textLength + 1),
                            "File argument should include the extension",
                            ProblemHighlightType.GENERIC_ERROR,
                            isOntheFly,
                            AddExtensionFix
                    ))
                }

        return descriptors
    }

    /**
     * See [LatexNoExtensionInspection.RemoveExtensionFix].
     */
    object AddExtensionFix : LocalQuickFix {

        override fun getFamilyName() = "Add file extension"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val document = command.containingFile.document() ?: return

            val replacement = Magic.Command.requiredExtensions[command.name]
                    ?.find { command.requiredParameter(0)?.endsWith(it) == false }
                    ?.run { command.requiredParameter(0)?.appendExtension(this) } ?: return

            // Exclude the enclosing braces
            val range = command.parameterList.first { it.requiredParam != null }.textRange.shiftRight(1).grown(-2)

            document.replaceString(range, replacement)
        }
    }
}