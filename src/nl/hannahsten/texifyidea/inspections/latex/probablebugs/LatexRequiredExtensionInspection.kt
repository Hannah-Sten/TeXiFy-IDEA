package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.inspections.AbstractTexifyCommandBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.SimpleFileInputContext
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.psi.contentText
import nl.hannahsten.texifyidea.psi.forEachRequiredParameter
import nl.hannahsten.texifyidea.util.parser.startOffsetInAncestor
import java.io.File
import java.util.*

/**
 * See [LatexNoExtensionInspection].
 */
class LatexRequiredExtensionInspection : AbstractTexifyCommandBasedInspection(
    inspectionId = "RequiredExtension",
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
        val requiredAnyExtension = findRequiredExtensions(command) ?: return

        command.forEachRequiredParameter {
            val params = it.contentText().split(",")
            var offset = it.startOffsetInAncestor(command)
            for (parameter in params) {
                offset += 1 // account for opening brace or comma
                val missingExtension = requiredAnyExtension.all { ext ->
                    !parameter.endsWith(".$ext") && !parameter.endsWith('}')
                }
                if (missingExtension) {
                    descriptors.add(
                        manager.createProblemDescriptor(
                            command,
                            TextRange(offset, offset + parameter.length),
                            "File argument should include the extension",
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOnTheFly,
                            AddExtensionFix
                        )
                    )
                }
                offset += parameter.length
            }
        }
    }

    object AddExtensionFix : LocalQuickFix {
        override fun getFamilyName() = "Add file extension for parameters"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands

            val requiredExtensions = findRequiredExtensions(command) ?: return
            val directory = command.containingFile.virtualFile.parent

            command.forEachRequiredParameter { parameter ->
                val params = parameter.contentText().split(",")
                val replacementText = params.joinToString {
                    requiredExtensions.firstNotNullOfOrNull { ext ->
                        if (!it.endsWith(".$ext") && !it.endsWith('}')) {
                            directory.findFileByRelativePath("$it.$ext")?.path?.removePrefix(directory.path)?.removePrefix(File.separator)
                        }
                        else null
                    } ?: it
                }
                val replacementElement = LatexPsiHelper(project).createRequiredParameter(replacementText)
                parameter.parent.node.replaceChild(parameter.node, replacementElement.node)
            }
        }
    }
}

private fun findRequiredExtensions(command: LatexCommands): List<String>? = LatexDefinitionService.resolveCommand(command)
    ?.arguments?.firstOrNull { it.name in setOf("bibliographyfile", "resource") }
    ?.contextSignature?.introducedContexts
    ?.filter { it is SimpleFileInputContext && it.isExtensionRequired }
    ?.flatMap { (it as SimpleFileInputContext).supportedExtensions }