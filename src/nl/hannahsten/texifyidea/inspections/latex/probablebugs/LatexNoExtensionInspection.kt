package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.inspections.AbstractTexifyCommandBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.startOffsetInAncestor
import java.util.*

/**
 * @author Sten Wessel
 */
class LatexNoExtensionInspection : AbstractTexifyCommandBasedInspection(
    inspectionId = "NoExtension",
    skipChildrenInContext = setOf(LatexContexts.Comment, LatexContexts.InsideDefinition)
) {

    override val outerSuppressionScopes: Set<MagicCommentScope>
        get() = EnumSet.of(MagicCommentScope.GROUP)

    override fun inspectCommand(
        command: LatexCommands,
        contexts: LContextSet,
        defBundle: DefinitionBundle,
        file: PsiFile,
        manager: InspectionManager,
        isOnTheFly: Boolean,
        descriptors: MutableList<ProblemDescriptor>
    ) {
        val nameWithSlash = command.nameWithSlash
        val illegalExtensions = CommandMagic.illegalExtensions[nameWithSlash] ?: return
        command.forEachRequiredParameter {
            val params = it.contentText().split(",")
            // assume all parameter are comma separated
            var offset = it.startOffsetInAncestor(command)
            for (parameter in params) {
                offset += 1 // account for opening brace or comma
                val hasIllegal = illegalExtensions.any { ext ->
                    parameter.endsWith(ext) && !parameter.endsWith('}')
                }
                if (hasIllegal) {
                    descriptors.add(
                        manager.createProblemDescriptor(
                            command,
                            TextRange(offset, offset + parameter.length),
                            "File argument should not include the extension",
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOnTheFly,
                            RemoveExtensionFix
                        )
                    )
                }
                offset += parameter.length
            }
        }
    }

    object RemoveExtensionFix : LocalQuickFix {
        override fun getFamilyName() = "Remove file extension from parameters"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands

            val nameWithSlash = command.nameWithSlash
            val illegalExtensions = CommandMagic.illegalExtensions[nameWithSlash] ?: return

            command.forEachRequiredParameter { parameter ->
                val params = parameter.contentText().split(",")
                val newParams = mutableListOf<String>()
                for (parameter in params) {
                    if (illegalExtensions.any { ext -> parameter.endsWith(ext) && !parameter.endsWith('}') }) {
                        val replacement = illegalExtensions
                            .find { ext -> parameter.endsWith(ext) }
                            ?.let { ext -> parameter.removeSuffix(ext) }
                            ?: parameter
                        newParams.add(replacement)
                    }
                    else {
                        newParams.add(parameter)
                    }
                }
                val newParameter = LatexPsiHelper(project).createRequiredParameter(newParams.joinToString(","))
                parameter.parent.node.replaceChild(parameter.node, newParameter.node)
            }
        }
    }
}