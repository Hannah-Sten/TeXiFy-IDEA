package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.lang.LSemanticEntity
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment

/**
 * Warn when the user uses a command that is not defined in any included packages or LaTeX base.
 * This is an extension of [LatexMissingImportInspection], however, because this also
 * complains about commands that are not hardcoded in TeXiFy but come from any package,
 * and this index of commands is far from complete, it has to be disabled by default,
 * and thus cannot be included in the mentioned inspection.
 *
 * @author Thomas
 */
class LatexUndefinedCommandInspection : LatexMissingImportInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "UndefinedCommand"

    override fun getDisplayName() = "Command is not defined"

    override fun reportUnknownCommand(
        command: LatexCommands, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    ) {
        descriptors.add(
            manager.createProblemDescriptor(
                command,
                TextRange(0, command.commandToken.textLength),
                "Undefined command: ${command.name}",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOntheFly
            )
        )
    }

    override fun reportUnknownEnvironment(
        name: String, environment: LatexEnvironment, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    ) {
        descriptors.add(
            manager.createProblemDescriptor(
                environment,
                TextRange(7, 7 + name.length),
                "Undefined environment: $name",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOntheFly,
            )
        )
    }

    override fun reportCommandMissingImport(command: LatexCommands, candidates: List<LSemanticEntity>, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean) {
    }

    override fun reportEnvironmentMissingImport(environment: LatexEnvironment, candidates: List<LSemanticEntity>, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean) {
    }
}