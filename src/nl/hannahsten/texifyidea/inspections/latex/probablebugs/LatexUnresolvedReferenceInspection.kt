package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.labels.findLatexAndBibtexLabelStringsInFileSet
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import java.lang.Integer.max
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class LatexUnresolvedReferenceInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "UnresolvedReference"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.COMMAND, MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Unresolved reference"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val labels = file.findLatexAndBibtexLabelStringsInFileSet()
        val commands = file.commandsInFile()

        for (command in commands) {
            if (!CommandMagic.reference.contains(command.name)) {
                continue
            }

            // Don't resolve references in command definitions, as in \cite{#1} the #1 is not a reference
            if (command.parent?.firstParentOfType(LatexCommands::class)?.name in CommandMagic.commandDefinitionsAndRedefinitions) {
                continue
            }

            val required = command.getRequiredParameters()
            if (required.isEmpty()) {
                continue
            }

            val parts = required[0].split(",")
            for (i in parts.indices) {
                val part = parts[i]
                if (part == "*") continue

                // The cleveref package allows empty items to customize enumerations
                if (part.isEmpty() && (command.name == LatexGenericRegularCommand.CREF.cmd || command.name == LatexGenericRegularCommand.CREF_CAPITAL.cmd)) continue

                // If there is no label with this required label parameter value
                if (!labels.contains(part.trim())) {
                    // We have to subtract from the total length, because we do not know whether optional
                    // parameters were included with [a][b][c] or [a,b,c] in which case the
                    // indices of the parts are different with respect to the start of the command
                    var offset = command.textLength - parts.sumOf { it.length + 1 }
                    for (j in 0 until i) {
                        offset += parts[j].length + 1
                    }

                    descriptors.add(
                        manager.createProblemDescriptor(
                            command,
                            TextRange.from(max(offset, 0), part.length),
                            "Unresolved reference '$part'",
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOntheFly
                        )
                    )
                }
            }
        }

        return descriptors
    }
}