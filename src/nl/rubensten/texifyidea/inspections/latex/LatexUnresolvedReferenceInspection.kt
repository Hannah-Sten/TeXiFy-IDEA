package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.util.Magic
import nl.rubensten.texifyidea.util.commandsInFile
import nl.rubensten.texifyidea.util.findLabelsInFileSet
import nl.rubensten.texifyidea.util.hasStar

/**
 * @author Ruben Schellekens
 */
open class LatexUnresolvedReferenceInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override fun getDisplayName() = "Unresolved reference"

    override val inspectionId = "UnresolvedReference"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val labels = file.findLabelsInFileSet()
        val commands = file.commandsInFile()
        for (command in commands) {
            if (!Magic.Command.reference.contains(command.name)) {
                continue
            }

            val required = command.requiredParameters
            if (required.isEmpty()) {
                continue
            }

            val parts = required[0].split(",")
            for (i in 0 until parts.size) {
                val part = parts[i]
                if (part == "*") continue

                if (!labels.contains(part.trim())) {
                    var offset = command.name!!.length + 1
                    for (j in 0 until i) {
                        offset += parts[j].length + 1
                    }

                    // Add offset change by optional parameters.
                    offset += command.optionalParameters.sumBy { it.length + 2 }

                    // Add extra star offset
                    if (command.hasStar()) {
                        offset++
                    }

                    descriptors.add(manager.createProblemDescriptor(
                            command,
                            TextRange.from(offset, part.length),
                            "Unresolved reference '$part'",
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOntheFly
                    ))
                }
            }
        }

        return descriptors
    }
}