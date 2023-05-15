package nl.hannahsten.texifyidea.inspections.latex.probablebugs

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
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.replaceString
import java.util.*

/**
 * @author Sten Wessel
 */
open class LatexNoExtensionInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "NoExtension"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "File argument should not include the extension"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        file.commandsInFile().asSequence()
            .filter { it.name in CommandMagic.illegalExtensions }
            .filter { command ->
                CommandMagic.illegalExtensions[command.name]!!.any {
                        extension ->
                    command.requiredParameters.any { it.split(",").any { parameter -> parameter.endsWith(extension) } }
                }
            }
            .forEach { command ->
                val parameterList = command.requiredParameters.map { it.split(",") }.flatten()
                var offset = command.parameterList.first { it.requiredParam != null }.textOffset - command.textOffset + 1
                for (parameter in parameterList) {
                    if (CommandMagic.illegalExtensions[command.name]!!.any { parameter.endsWith(it) }) {
                        descriptors.add(
                            manager.createProblemDescriptor(
                                command,
                                TextRange(offset, offset + parameter.length),
                                "File argument should not include the extension",
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                isOntheFly,
                                RemoveExtensionFix
                            )
                        )
                    }

                    // Assume all parameter are comma separated
                    offset += parameter.length + ",".length
                }
            }

        return descriptors
    }

    /**
     * @author Sten Wessel
     */
    object RemoveExtensionFix : LocalQuickFix {

        override fun getFamilyName() = "Remove file extension from parameters"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val document = command.containingFile.document() ?: return

            val parameterList = command.requiredParameters.map { it.split(",") }.flatten()
            var offset = 0
            for (parameter in parameterList) {
                if (CommandMagic.illegalExtensions.getOrDefault(command.name, null)?.any { parameter.endsWith(it) } == true) {
                    val range = TextRange(offset, offset + parameter.length).shiftRight(command.parameterList.first { it.requiredParam != null }.textOffset + 1)
                    val replacement = CommandMagic.illegalExtensions[command.name]
                        ?.find { parameter.endsWith(it) }
                        ?.run { parameter.removeSuffix(this) } ?: break
                    document.replaceString(range, replacement)

                    // Maintain offset for any removed part
                    offset -= (range.length - replacement.length)
                }

                // Assume all parameter are comma separated
                offset += parameter.length + ",".length
            }
        }
    }
}